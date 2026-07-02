package com.androiddrop.data.transfer

import com.androiddrop.core.common.Constants
import com.androiddrop.core.crypto.CryptoManager
import com.androiddrop.core.crypto.SessionKeys
import com.androiddrop.core.network.SocketManager
import com.androiddrop.domain.model.FileNode
import com.androiddrop.domain.model.NearbyDevice
import com.androiddrop.domain.model.TransferSession
import com.androiddrop.domain.model.TransferStatus
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class TransferSessionManager @Inject constructor(
    private val cryptoManager: CryptoManager
) {

    data class ActiveSession(
        val sessionId: String,
        val session: TransferSession,
        val socket: SocketManager.ManagedSocket,
        val keys: SessionKeys,
        val startTime: Long
    )

    val activeSessions: MutableMap<String, ActiveSession> = ConcurrentHashMap()

    suspend fun createSession(
        file: FileNode,
        targetDevice: NearbyDevice,
        socket: SocketManager.ManagedSocket
    ): ActiveSession {
        val sessionId = UUID.randomUUID().toString()
        Timber.d("Creando sesión de transferencia: $sessionId")

        val keyPair = cryptoManager.generateKeyPair()
        val sharedSecret = cryptoManager.deriveSharedSecret(targetDevice.deviceId.toByteArray())
        val sessionKeys = cryptoManager.deriveSessionKeys(sharedSecret)

        Timber.d("Claves ECDH negociadas para sesión $sessionId")

        val now = System.currentTimeMillis()
        val transferSession = TransferSession(
            sessionId = sessionId,
            file = file,
            senderDeviceId = "local",
            receiverDeviceId = targetDevice.deviceId,
            status = TransferStatus.PENDING,
            createdAt = now,
            updatedAt = now
        )

        val activeSession = ActiveSession(
            sessionId = sessionId,
            session = transferSession,
            socket = socket,
            keys = sessionKeys,
            startTime = System.currentTimeMillis()
        )

        activeSessions[sessionId] = activeSession
        Timber.d("Sesión $sessionId creada con claves derivadas")
        return activeSession
    }

    fun removeSession(sessionId: String) {
        activeSessions.remove(sessionId)?.let {
            Timber.d("Sesión eliminada: $sessionId")
        }
    }
}
