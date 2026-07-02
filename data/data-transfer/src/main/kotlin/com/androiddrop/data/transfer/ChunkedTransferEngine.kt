package com.androiddrop.data.transfer

import com.androiddrop.core.common.Constants
import com.androiddrop.core.crypto.CryptoManager
import com.androiddrop.domain.model.TransferProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.security.MessageDigest
import javax.inject.Inject
import kotlin.math.min

class ChunkedTransferEngine @Inject constructor(
    private val cryptoManager: CryptoManager,
    private val sessionManager: TransferSessionManager
) {

    companion object {
        private const val CHUNK_SIZE = Constants.CHUNK_SIZE
        private const val PARALLEL_STREAMS = 4
    }

    fun sendFile(
        session: TransferSessionManager.ActiveSession,
        file: com.androiddrop.domain.model.FileNode
    ): Flow<TransferProgress> = flow {
        val fileHandle = File(file.path)
        if (!fileHandle.exists()) {
            throw IllegalStateException("Archivo no encontrado: ${file.path}")
        }

        val fileSize = fileHandle.length()
        val totalChunks = ((fileSize + CHUNK_SIZE - 1) / CHUNK_SIZE).toInt()
        var bytesTransferred = 0L
        var startTime = System.currentTimeMillis()

        Timber.d("Iniciando envío de ${file.name} (%d chunks, %d bytes)", totalChunks, fileSize)

        val randomAccessFile = RandomAccessFile(fileHandle, "r")
        val channel = randomAccessFile.channel
        val digest = MessageDigest.getInstance("SHA-256")

        try {
            for (chunkIndex in 0 until totalChunks) {
                val position = chunkIndex.toLong() * CHUNK_SIZE
                val chunkSize = min(CHUNK_SIZE.toLong(), fileSize - position).toInt()

                val buffer = ByteBuffer.allocateDirect(chunkSize)
                channel.read(buffer, position)
                buffer.flip()

                val plaintext = ByteArray(chunkSize)
                buffer.get(plaintext)

                digest.update(plaintext)

                val encrypted = cryptoManager.encrypt(plaintext, session.keys, chunkIndex.toLong())

                session.socket.send(encrypted)

                bytesTransferred += chunkSize
                val elapsed = System.currentTimeMillis() - startTime
                val speed = if (elapsed > 0) (bytesTransferred * 1000) / elapsed else 0L
                val remaining = if (speed > 0) ((fileSize - bytesTransferred) * 1000) / speed else 0L

                emit(
                    TransferProgress.Transferring(
                        bytesTransferred = bytesTransferred,
                        totalBytes = fileSize,
                        speedBps = speed,
                        estimatedTimeRemainingMs = remaining,
                        currentChunk = chunkIndex + 1,
                        totalChunks = totalChunks
                    )
                )
            }

            val fileChecksum = digest.digest()
            Timber.d(
                "Archivo enviado. SHA-256: %s",
                fileChecksum.joinToString("") { "%02x".format(it) }
            )
        } catch (e: Exception) {
            Timber.e(e, "Error durante envío de archivo")
            throw e
        } finally {
            channel.close()
            randomAccessFile.close()
        }
    }

    suspend fun receiveFile(
        session: TransferSessionManager.ActiveSession,
        metadata: FileMetadata
    ): Flow<TransferProgress> = flow {
        val outputFile = File(session.session.file.path)
        val parentDir = outputFile.parentFile
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs()
        }

        val fileSize = metadata.size
        val totalChunks = metadata.totalChunks
        var bytesTransferred = 0L
        var startTime = System.currentTimeMillis()

        Timber.d("Iniciando recepción de %s (%d chunks)", metadata.name, totalChunks)

        val randomAccessFile = RandomAccessFile(outputFile, "rw")
        val channel = randomAccessFile.channel
        val digest = MessageDigest.getInstance("SHA-256")

        try {
            var chunkIndex = 0
            session.socket.receiveFlow().collect { encryptedData ->
                if (chunkIndex >= totalChunks) return@collect

                val plaintext = cryptoManager.decrypt(encryptedData, session.keys, chunkIndex.toLong())
                digest.update(plaintext)

                val buffer = ByteBuffer.allocateDirect(plaintext.size)
                buffer.put(plaintext)
                buffer.flip()

                val position = chunkIndex.toLong() * CHUNK_SIZE
                channel.write(buffer, position)

                bytesTransferred += plaintext.size
                val elapsed = System.currentTimeMillis() - startTime
                val speed = if (elapsed > 0) (bytesTransferred * 1000) / elapsed else 0L
                val remaining = if (speed > 0) ((fileSize - bytesTransferred) * 1000) / speed else 0L

                emit(
                    TransferProgress.Transferring(
                        bytesTransferred = bytesTransferred,
                        totalBytes = fileSize,
                        speedBps = speed,
                        estimatedTimeRemainingMs = remaining,
                        currentChunk = chunkIndex + 1,
                        totalChunks = totalChunks
                    )
                )

                chunkIndex++
            }

            val receivedChecksum = digest.digest()
            val checksumMatch = receivedChecksum.contentEquals(metadata.checksum)

            if (checksumMatch) {
                Timber.d("Checksum SHA-256 verificado correctamente")
            } else {
                Timber.w("Checksum SHA-256 no coincide")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error durante recepción de archivo")
            throw e
        } finally {
            channel.close()
            randomAccessFile.close()
        }
    }
}
