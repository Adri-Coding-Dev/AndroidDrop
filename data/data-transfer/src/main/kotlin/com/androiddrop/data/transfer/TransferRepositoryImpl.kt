package com.androiddrop.data.transfer

import com.androiddrop.core.network.TcpSocketManager
import com.androiddrop.domain.model.FileNode
import com.androiddrop.domain.model.NearbyDevice
import com.androiddrop.domain.model.TransferProgress
import com.androiddrop.domain.model.TransferSession
import com.androiddrop.domain.model.TransferStatus
import com.androiddrop.domain.repository.TransferRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class TransferRepositoryImpl @Inject constructor(
    private val sessionManager: TransferSessionManager,
    private val engine: ChunkedTransferEngine
) : TransferRepository {

    private val sessions = ConcurrentHashMap<String, TransferSession>()
    private val sessionsFlow = MutableStateFlow<Map<String, TransferSession>>(emptyMap())

    override suspend fun createSession(
        file: FileNode,
        targetDevice: NearbyDevice
    ): TransferSession {
        val now = System.currentTimeMillis()
        val session = TransferSession(
            sessionId = UUID.randomUUID().toString(),
            file = file,
            senderDeviceId = "local", // Placeholder
            receiverDeviceId = targetDevice.deviceId,
            status = TransferStatus.PENDING,
            createdAt = now,
            updatedAt = now
        )
        sessions[session.sessionId] = session
        sessionsFlow.value = sessions.toMap()
        Timber.d("Sesión creada: ${session.sessionId}")
        return session
    }

    override fun getSession(sessionId: String): Flow<TransferSession?> {
        return sessionsFlow.map { it[sessionId] }
    }

    override fun transferFile(session: TransferSession): Flow<TransferProgress> {
        val sessionId = session.sessionId
        updateSessionStatus(sessionId, TransferStatus.TRANSFERRING)

        val activeSession = sessionManager.activeSessions[sessionId]
            ?: throw IllegalStateException("Sesión activa no encontrada: $sessionId")

        return engine.sendFile(activeSession, session.file)
    }

    override suspend fun cancelTransfer(sessionId: String) {
        updateSessionStatus(sessionId, TransferStatus.CANCELLED)
        sessionManager.removeSession(sessionId)
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
