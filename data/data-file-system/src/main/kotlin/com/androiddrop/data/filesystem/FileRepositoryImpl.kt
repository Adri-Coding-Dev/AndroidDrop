package com.androiddrop.data.filesystem

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import com.androiddrop.domain.model.FileNode
import com.androiddrop.domain.repository.FileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FileRepository {

    override fun getFiles(directoryUri: String): Flow<List<FileNode>> = flow {
        try {
            val results = queryFilesByUri(directoryUri)
            emit(results)
        } catch (e: Exception) {
            Timber.e(e, "Error al listar archivos en $directoryUri")
            try {
                val p = when {
                    directoryUri.startsWith("/") -> directoryUri
                    directoryUri.startsWith("file://") -> Uri.parse(directoryUri).path
                    else -> null
                }
                val fallback = if (p != null) {
                    queryMediaStoreFilesAndDirs(p)
                } else {
                    queryMediaStoreRoot()
                }
                emit(fallback.ifEmpty { queryMediaStoreRoot() })
            } catch (e2: Exception) {
                Timber.e(e2, "Fallback también falló")
                emit(queryMediaStoreRoot())
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun queryFilesByUri(directoryUri: String): List<FileNode> {
        val results = mutableListOf<FileNode>()

        when {
            directoryUri.startsWith("content://media/") -> {
                results.addAll(queryMediaStoreFiles(Uri.parse(directoryUri)))
            }
            directoryUri.startsWith("content://") -> {
                results.addAll(querySAFFiles(Uri.parse(directoryUri)))
            }
            directoryUri.startsWith("file://") -> {
                val filePath = Uri.parse(directoryUri).path
                if (filePath != null) {
                    results.addAll(queryDirectoryContents(filePath))
                }
            }
            directoryUri.startsWith("/") -> {
                results.addAll(queryDirectoryContents(directoryUri))
            }
            else -> {
                results.addAll(queryDirectoryContents(
                    Environment.getExternalStorageDirectory().absolutePath
                ))
            }
        }

        return results
    }

    private fun queryDirectoryContents(path: String): List<FileNode> {
        val resultMap = linkedMapOf<String, FileNode>()

        // 1. Siempre mostrar directorios comunes en la raíz
        val isRoot = path == Environment.getExternalStorageDirectory().absolutePath
        if (isRoot) {
            for (item in queryMediaStoreRoot()) {
                resultMap[item.uri] = item
            }
        }

        // 2. Intentar listado por sistema de archivos
        for (item in queryFileSystemFiles(path)) {
            resultMap[item.uri] = item
        }

        // 3. Complementar con MediaStore (archivos y directorios virtuales)
        for (item in queryMediaStoreFilesAndDirs(path)) {
            if (item.uri !in resultMap) {
                resultMap[item.uri] = item
            }
        }

        return resultMap.values.toList()
    }

    private fun querySAFFiles(uri: Uri): List<FileNode> {
        val results = mutableListOf<FileNode>()
        val treeId = DocumentsContract.getTreeDocumentId(uri)
        val uriTree = DocumentsContract.buildChildDocumentsUriUsingTree(uri, treeId)

        context.contentResolver.query(uriTree, null, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val documentId = cursor.getString(
                    cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                )
                val name = cursor.getString(
                    cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                ) ?: "unknown"
                val mime = cursor.getString(
                    cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                ) ?: "application/octet-stream"
                val size = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)
                )
                val lastModified = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                )

                val childUri = DocumentsContract.buildDocumentUriUsingTree(uri, documentId)

                results.add(
                    FileNode(
                        uri = childUri.toString(),
                        name = name,
                        path = childUri.path ?: "",
                        extension = name.substringAfterLast('.', ""),
                        size = size,
                        mimeType = mime,
                        isDirectory = DocumentsContract.Document.MIME_TYPE_DIR == mime,
                        lastModified = lastModified
                    )
                )
            }
        }
        return results
    }

    private fun queryMediaStoreFiles(uri: Uri): List<FileNode> {
        val path = uri.toString()
        val parentPath = if (path.endsWith("/root") || path == "content://media/external") {
            Environment.getExternalStorageDirectory().absolutePath
        } else {
            uri.lastPathSegment?.let { Uri.decode(it) } ?: return emptyList()
        }
        return executeMediaStoreQuery(parentPath)
    }

    private fun queryMediaStoreFilesForPath(filePath: String): List<FileNode> {
        return executeMediaStoreQuery(filePath)
    }

    private fun executeMediaStoreQuery(parentPath: String): List<FileNode> {
        val results = mutableListOf<FileNode>()

        val collectionUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )

        val selection = "${MediaStore.Files.FileColumns.DATA} LIKE ? AND " +
                "${MediaStore.Files.FileColumns.DATA} NOT LIKE ?"
        val selectionArgs = arrayOf("$parentPath/%", "$parentPath/%/%")

        val seen = mutableSetOf<Long>()

        context.contentResolver.query(collectionUri, projection, selection, selectionArgs, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                val displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)) ?: continue
                val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)) ?: "application/octet-stream"
                val size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE))
                val dateModified = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)) * 1000
                val data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))

                if (!seen.add(id)) continue

                val fileUri = if (data != null) {
                    Uri.fromFile(File(data)).toString()
                } else {
                    ContentUris.withAppendedId(collectionUri, id).toString()
                }

                results.add(
                    FileNode(
                        uri = fileUri,
                        name = displayName,
                        path = data ?: "$parentPath/$displayName",
                        extension = displayName.substringAfterLast('.', ""),
                        size = size,
                        mimeType = mimeType,
                        isDirectory = false,
                        lastModified = dateModified
                    )
                )
            }
        }

        return results
    }

    private fun queryMediaStoreFilesAndDirs(path: String): List<FileNode> {
        val results = mutableListOf<FileNode>()
        val collectionUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val seenIds = mutableSetOf<Long>()
        val seenDirs = mutableSetOf<String>()

        // 1. Obtener archivos directamente en este directorio
        val filesQuery = "${MediaStore.Files.FileColumns.DATA} LIKE ? AND " +
                "${MediaStore.Files.FileColumns.DATA} NOT LIKE ?"
        val filesArgs = arrayOf("$path/%", "$path/%/%")

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DATA
        )

        context.contentResolver.query(collectionUri, projection, filesQuery, filesArgs, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                val displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)) ?: continue
                val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)) ?: "application/octet-stream"
                val size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE))
                val dateModified = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)) * 1000
                val data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))

                if (!seenIds.add(id)) continue

                // DATA column is deprecated on API 29+ and returns null on API 30+
                // Use ContentUris when DATA is not available
                val fileUri = if (data != null) {
                    Uri.fromFile(File(data)).toString()
                } else {
                    ContentUris.withAppendedId(collectionUri, id).toString()
                }

                results.add(
                    FileNode(
                        uri = fileUri,
                        name = displayName,
                        path = data ?: "$path/$displayName",
                        extension = displayName.substringAfterLast('.', ""),
                        size = size,
                        mimeType = mimeType,
                        isDirectory = false,
                        lastModified = dateModified
                    )
                )
            }
        }

        // 2. Extraer subdirectorios virtuales (solo si DATA column está disponible)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            val dirQuery = "${MediaStore.Files.FileColumns.DATA} LIKE ?"
            val dirArgs = arrayOf("$path/%/%")

            context.contentResolver.query(collectionUri, arrayOf(MediaStore.Files.FileColumns.DATA), dirQuery, dirArgs, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    val data = cursor.getString(0) ?: continue
                    val relative = data.removePrefix("$path/")
                    val childDirName = relative.substringBefore("/")
                    if (childDirName.isNotEmpty() && seenDirs.add(childDirName)) {
                        val dirPath = "$path/$childDirName"
                        val dirFile = File(dirPath)
                        results.add(
                            FileNode(
                                uri = Uri.fromFile(dirFile).toString(),
                                name = childDirName,
                                path = dirPath,
                                extension = "",
                                size = 0L,
                                mimeType = DocumentsContract.Document.MIME_TYPE_DIR,
                                isDirectory = true,
                                lastModified = if (dirFile.exists()) dirFile.lastModified() else System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
        }

        return results
    }

    private fun queryMediaStoreRoot(): List<FileNode> {
        val directories = listOf(
            Environment.DIRECTORY_DOWNLOADS,
            Environment.DIRECTORY_DOCUMENTS,
            Environment.DIRECTORY_PICTURES,
            Environment.DIRECTORY_MOVIES,
            Environment.DIRECTORY_MUSIC,
            Environment.DIRECTORY_DCIM
        )
        return directories.mapNotNull { dir ->
            val path = Environment.getExternalStoragePublicDirectory(dir).absolutePath
            val file = File(path)
            if (file.exists()) {
                FileNode(
                    uri = Uri.fromFile(file).toString(),
                    name = file.name,
                    path = path,
                    extension = "",
                    size = 0L,
                    mimeType = DocumentsContract.Document.MIME_TYPE_DIR,
                    isDirectory = true,
                    lastModified = file.lastModified()
                )
            } else null
        }
    }

    private fun queryFileSystemFiles(path: String): List<FileNode> {
        val results = mutableListOf<FileNode>()
        val dir = File(path)
        if (!dir.exists() || !dir.isDirectory) return results

        val children = dir.listFiles() ?: return results
        for (file in children.sortedWith(compareByDescending<File> { it.isDirectory }.thenBy { it.name.lowercase() })) {
            results.add(
                FileNode(
                    uri = Uri.fromFile(file).toString(),
                    name = file.name,
                    path = file.absolutePath,
                    extension = file.name.substringAfterLast('.', ""),
                    size = if (file.isFile) file.length() else 0L,
                    mimeType = if (file.isDirectory) DocumentsContract.Document.MIME_TYPE_DIR
                              else getMimeType(file.name),
                    isDirectory = file.isDirectory,
                    lastModified = file.lastModified()
                )
            )
        }
        return results
    }

    private fun getMimeType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "zip" -> "application/zip"
            "apk" -> "application/vnd.android.package-archive"
            else -> "application/octet-stream"
        }
    }

    override fun getFileTree(rootUri: String): Flow<FileNode> = flow {
        // Simple implementation for now
        val info = getFileInfo(rootUri)
        emit(info)
    }

    override suspend fun getThumbnail(uri: String, maxSize: Int): ByteArray? = withContext(Dispatchers.IO) {
        // Placeholder
        null
    }

    override suspend fun getFileInfo(uri: String): FileNode = withContext(Dispatchers.IO) {
        val u = Uri.parse(uri)
        val docFile = androidx.documentfile.provider.DocumentFile.fromSingleUri(context, u)
            ?: throw Exception("No se pudo obtener información de $uri")

        FileNode(
            uri = uri,
            name = docFile.name ?: "unknown",
            path = u.path ?: "",
            extension = docFile.name?.substringAfterLast('.', "") ?: "",
            size = docFile.length(),
            mimeType = docFile.type ?: "application/octet-stream",
            isDirectory = docFile.isDirectory,
            lastModified = docFile.lastModified()
        )
    }

    override suspend fun exists(uri: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val docFile = androidx.documentfile.provider.DocumentFile.fromSingleUri(context, Uri.parse(uri))
            docFile?.exists() ?: false
        } catch (e: Exception) {
            Timber.e(e, "Error al verificar existencia de $uri")
            false
        }
    }
}
