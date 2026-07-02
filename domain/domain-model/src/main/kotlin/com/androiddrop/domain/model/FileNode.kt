package com.androiddrop.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FileNode(
    val uri: String,
    val name: String,
    val path: String,
    val extension: String,
    val size: Long,
    val isDirectory: Boolean,
    val mimeType: String,
    val lastModified: Long,
    val children: List<FileNode> = emptyList()
) {
    val formattedSize: String
        get() = size.formattedSize

    val category: FileCategory
        get() = when {
            isDirectory -> FileCategory.FOLDER
            mimeType.startsWith("image/") -> FileCategory.IMAGE
            mimeType.startsWith("video/") -> FileCategory.VIDEO
            mimeType.startsWith("audio/") -> FileCategory.AUDIO
            mimeType == "application/pdf" ||
            mimeType == "text/plain" ||
            mimeType == "application/msword" ||
            mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
            mimeType == "application/vnd.ms-excel" ||
            mimeType == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ||
            mimeType == "application/vnd.ms-powerpoint" ||
            mimeType == "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> FileCategory.DOCUMENT
            mimeType == "application/zip" ||
            mimeType == "application/x-rar-compressed" ||
            mimeType == "application/gzip" -> FileCategory.ARCHIVE
            extension == "apk" -> FileCategory.APK
            else -> FileCategory.OTHER
        }
}

enum class FileCategory {
    FOLDER, IMAGE, VIDEO, AUDIO, DOCUMENT, ARCHIVE, APK, OTHER
}

val Long.formattedSize: String
    get() {
        if (this == 0L) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val base = 1024.0
        val unitIndex = (Math.log(toDouble()) / Math.log(base)).toInt().coerceAtMost(units.size - 1)
        val formatted = toDouble() / Math.pow(base, unitIndex.toDouble())
        return if (unitIndex == 0) {
            "$this B"
        } else {
            "%.1f %s".format(formatted, units[unitIndex])
        }
    }
