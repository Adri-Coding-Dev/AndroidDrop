package com.androiddrop.data.wifidirect

import com.androiddrop.core.common.Constants
import com.androiddrop.core.network.SocketManager
import com.androiddrop.core.network.TcpSocketManager
import com.androiddrop.domain.model.FileNode
import com.androiddrop.domain.model.NearbyDevice
import com.androiddrop.domain.model.TransferProgress
import com.androiddrop.domain.model.TransferSession
import com.androiddrop.domain.model.TransferStatus
import com.androiddrop.domain.repository.TransferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.math.min

class WiFiDirectTransferRepository @Inject constructor(
    private val wifiDirectManager: WiFiDirectManager,
    private val socketManager: TcpSocketManager
) : TransferRepository {

    private val sessions = ConcurrentHashMap<String, TransferSession>()
    private val sessionsFlow = MutableStateFlow<Map<String, TransferSession>>(emptyMap())

    companion object {
        private const val CHUNK_SIZE = Constants.CHUNK_SIZE
        private const val PARALLEL_SOCKETS = 4
        private const val TRANSFER_PORT = 57234
    }

    private val serverSockets = mutableListOf<ServerSocket>()
    private val clientSockets = mutableListOf<Socket>()

    override suspend fun createSession(file: FileNode, targetDevice: NearbyDevice): TransferSession {
        val now = System.currentTimeMillis()
        val session = TransferSession(
            sessionId = UUID.randomUUID().toString(),
            file = file,
            senderDeviceId = "local",
            receiverDeviceId = targetDevice.deviceId,
            status = TransferStatus.PENDING,
            createdAt = now,
            updatedAt = now
        )
        sessions[session.sessionId] = session
        sessionsFlow.value = sessions.toMap()
        Timber.d("Sesión de transferencia creada: ${session.sessionId}")
        return session
    }

    override fun getSession(sessionId: String): Flow<TransferSession?> {
        return sessionsFlow.map { it[sessionId] }
    }

    override fun transferFile(session: TransferSession): Flow<TransferProgress> = flow {
        val sessionId = session.sessionId
        updateSessionStatus(sessionId, TransferStatus.TRANSFERRING)

        val file = File(session.file.path)
        if (!file.exists()) {
            throw IllegalStateException("Archivo no encontrado: ${session.file.path}")
        }

        val fileSize = file.length()
        val totalChunks = ((fileSize + CHUNK_SIZE - 1) / CHUNK_SIZE).toInt()
        var bytesTransferred = 0L
        var startTime = System.currentTimeMillis()

        try {
            val serverSocket = ServerSocket(TRANSFER_PORT)
            serverSockets.add(serverSocket)

            val clientFile = java.io.RandomAccessFile(file, "r")
            val channel = clientFile.channel

            val digest = MessageDigest.getInstance("SHA-256")

            for (chunkIndex in 0 until totalChunks) {
                val socket = serverSocket.accept()
                clientSockets.add(socket)

                val position = chunkIndex.toLong() * CHUNK_SIZE
                val chunkSize = min(CHUNK_SIZE.toLong(), fileSize - position).toInt()

                val buffer = java.nio.ByteBuffer.allocateDirect(chunkSize)
                channel.read(buffer, position)
                buffer.flip()

                val chunkData = ByteArray(chunkSize)
                buffer.get(chunkData)
                digest.update(chunkData)

                val outputStream = socket.getOutputStream()
                val lengthPrefix = byteArrayOf(
                    (chunkSize shr 24).toByte(),
                    (chunkSize shr 16).toByte(),
                    (chunkSize shr 8).toByte(),
                    chunkSize.toByte()
                )
                outputStream.write(lengthPrefix)
                outputStream.write(chunkData)
                outputStream.flush()

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

                socket.close()
            }

            channel.close()
            clientFile.close()

            val checksum = digest.digest()
            Timber.d("Transferencia completada. SHA-256: ${checksum.joinToString("") { "%02x".format(it) }}")

            updateSessionStatus(sessionId, TransferStatus.COMPLETED)
        } catch (e: Exception) {
            Timber.e(e, "Error durante transferencia de archivo")
            updateSessionStatus(sessionId, TransferStatus.FAILED)
            throw e
        } finally {
            serverSockets.forEach { it.close() }
            serverSockets.clear()
            clientSockets.forEach { it.close() }
            clientSockets.clear()
        }
    }

    override suspend fun cancelTransfer(sessionId: String) {
        updateSessionStatus(sessionId, TransferStatus.CANCELLED)
        serverSockets.forEach { it.close() }
        serverSockets.clear()
        clientSockets.forEach { it.close() }
        clientSockets.clear()
        Timber.d("Transferencia cancelada: $sessionId")
    }

    override suspend fun verifyTransfer(sessionId: String): Boolean {
        val session = sessions[sessionId] ?: return false
        return session.status == TransferStatus.COMPLETED
    }

    override fun getActiveTransfers(): Flow<List<TransferSession>> {
        return sessionsFlow.map { map ->
            map.values.filter {
                it.status == TransferStatus.TRANSFERRING ||
                    it.status == TransferStatus.PENDING ||
                    it.status == TransferStatus.NEGOTIATING
            }
        }
    }

    private fun updateSessionStatus(sessionId: String, status: TransferStatus) {
        sessions.computeIfPresent(sessionId) { _, session ->
            session.copy(status = status, updatedAt = System.currentTimeMillis())
        }
        sessionsFlow.value = sessions.toMap()
    }
}
