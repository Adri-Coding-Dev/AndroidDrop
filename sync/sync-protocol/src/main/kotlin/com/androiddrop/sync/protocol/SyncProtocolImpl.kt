package com.androiddrop.sync.protocol

import com.androiddrop.core.network.SocketManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementación del protocolo de sincronización sobre TCP.
 *
 * POR QUÉ 20-30 Hz: Por debajo de 20 Hz la animación se siente entrecortada.
 * Por encima de 30 Hz el overhead de red empieza a ser significativo para
 * chunks de ~100 bytes por frame. 25 Hz es el punto dulce entre suavidad
 * y eficiencia de red (~2.5 KB/s por dirección).
 *
 * POR QUÉ clock maestro: En una conexión peer-to-peer, ambos peers no pueden
 * actuar como clock simultáneamente porque sus relojes no están sincronizados.
 * El emisor (quien envía el archivo) actúa como clock maestro. El receptor
 * ajusta su línea de tiempo basado en el RTT medido.
 *
 * @property socketManager Gestor de sockets para comunicación TCP.
 */
class SyncProtocolImpl @Inject constructor(
    private val socketManager: SocketManager
) : SyncProtocol {

    /** Serializador JSON para los frames de sincronización. */
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /** Offset del clock remoto (en nanosegundos). */
    private var remoteClockOffset: Long = 0L

    /** Número de secuencia para el próximo frame a enviar. */
    private var nextSequenceNumber: Int = 0

    /** Socket activo para la sincronización. */
    private var currentSocket: SocketManager.ManagedSocket? = null

    companion object {
        /** Frecuencia de envío de frames: 25 Hz (40ms entre frames). */
        const val SYNC_INTERVAL_MS = 40L

        /** Tamaño máximo de la cola de frames pendientes. */
        private const val MAX_PENDING_FRAMES = 10
    }

    /**
     * Configura el socket a usar para la sincronización.
     */
    fun setSocket(socket: SocketManager.ManagedSocket) {
        currentSocket = socket
    }

    override suspend fun sendSync(frame: SyncFrame) {
        val socket = currentSocket ?: run {
            Timber.w("SyncProtocol: no hay socket configurado")
            return
        }

        val frameWithSequence = frame.copy(
            sequenceNumber = nextSequenceNumber++,
            timestamp = System.nanoTime() + remoteClockOffset
        )

        val data = serializeFrame(frameWithSequence)
        socket.send(data)

        Timber.v("SyncProtocol: frame enviado (seq=${frameWithSequence.sequenceNumber})")
    }

    override fun receiveSync(): Flow<SyncFrame> {
        val socket = currentSocket ?: throw IllegalStateException("Socket no configurado")

        return socket.receiveFlow().map { data ->
            val frame = deserializeFrame(data)
            Timber.v("SyncProtocol: frame recibido (seq=${frame.sequenceNumber})")
            frame
        }
    }

    override suspend fun negotiateClock(): Long {
        Timber.d("SyncProtocol: negociando clock...")

        return withContext(Dispatchers.IO) {
            val socket = currentSocket ?: return@withContext 0L

            // Enviar ping y medir RTT
            val pingTime = System.nanoTime()
            val pingFrame = SyncFrame(
                timestamp = pingTime,
                sequenceNumber = nextSequenceNumber++,
                isKeyFrame = true
            )
            socket.send(serializeFrame(pingFrame))

            // Esperar respuesta (pong)
            val pongFrame = socket.receiveFlow()
                .map { deserializeFrame(it) }
                .first()

            val now = System.nanoTime()
            val rtt = now - pingTime
            remoteClockOffset = (pongFrame.timestamp - pingTime - rtt / 2)
            Timber.d("SyncProtocol: RTT = ${rtt / 1_000_000}ms, offset = ${remoteClockOffset / 1_000_000}ms")

            remoteClockOffset
        }
    }

    /**
     * Serializa un SyncFrame a JSON y luego a ByteArray.
     */
    private fun serializeFrame(frame: SyncFrame): ByteArray {
        return json.encodeToString(SyncFrame.serializer(), frame).encodeToByteArray()
    }

    /**
     * Deserializa un ByteArray a SyncFrame.
     */
    private fun deserializeFrame(data: ByteArray): SyncFrame {
        return json.decodeFromString(SyncFrame.serializer(), data.decodeToString())
    }
}
