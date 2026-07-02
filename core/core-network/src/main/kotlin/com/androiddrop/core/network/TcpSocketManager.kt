package com.androiddrop.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import timber.log.Timber

/**
 * Implementación de [SocketManager] sobre TCP/IP usando OkHttp y ServerSocket.
 *
 * POR QUÉ TCP vs UDP: TCP garantiza entrega ordenada y sin pérdida de paquetes,
 * requisito indispensable para transferencia de archivos donde un paquete perdido
 * corrompería el archivo completo. El overhead de TCP es aceptable para el
 * tamaño de chunk de 1MB.
 *
 * POR QUÉ OkHttp: OkHttp proporciona gestión eficiente de conexiones, timeout
 * configurable, y keep-alive. Aunque no usamos HTTP directamente, el OkHttpClient
 * se inyecta para consistencia con el resto del stack de red y para aprovechar
 * su pool de conexiones.
 *
 * @param okHttpClient Cliente OkHttp inyectado (usado para configuración de red).
 */
class TcpSocketManager @Inject constructor(
    private val okHttpClient: OkHttpClient
) : SocketManager {

    companion object {
        /** Timeout de conexión en segundos. */
        private const val CONNECT_TIMEOUT_SECONDS = 10L

        /** Timeout de lectura en segundos. */
        private const val READ_TIMEOUT_SECONDS = 30L

        /** Timeout de escritura en segundos. */
        private const val WRITE_TIMEOUT_SECONDS = 30L

        /** Tamaño del buffer de lectura en bytes (64KB). */
        private const val BUFFER_SIZE = 65536

        /** Puerto por defecto para conexiones AndroidDrop. */
        private const val DEFAULT_PORT = 57234
    }

    /** ServerSocket para aceptar conexiones entrantes. */
    private var serverSocket: ServerSocket? = null

    /**
     * Conecta a un peer remoto.
     *
     * @param connectionInfo Información del peer (IP, puerto).
     * @return ManagedSocket listo para comunicación.
     */
    override suspend fun connectTo(connectionInfo: ConnectionInfo): SocketManager.ManagedSocket {
        Timber.d("Conectando a ${connectionInfo.ipAddress}:${connectionInfo.port}")

        return withContext(Dispatchers.IO) {
            val socket = Socket()
            socket.connect(
                java.net.InetSocketAddress(connectionInfo.ipAddress, connectionInfo.port),
                (CONNECT_TIMEOUT_SECONDS * 1000).toInt()
            )
            socket.keepAlive = true
            socket.soTimeout = (READ_TIMEOUT_SECONDS * 1000).toInt()

            Timber.d("Conexión establecida con ${connectionInfo.deviceName}")
            TcpManagedSocket(socket)
        }
    }

    /**
     * Acepta una conexión entrante.
     *
     * Crea un ServerSocket en el puerto por defecto y espera una conexión.
     * Solo maneja una conexión a la vez (modelo 1:1 de AndroidDrop).
     *
     * @return ManagedSocket listo para comunicación con el peer conectado.
     */
    override suspend fun acceptConnection(): SocketManager.ManagedSocket {
        return withContext(Dispatchers.IO) {
            val server = ServerSocket(DEFAULT_PORT)
            serverSocket = server
            server.soTimeout = (CONNECT_TIMEOUT_SECONDS * 1000).toInt()

            Timber.d("Esperando conexión entrante en puerto $DEFAULT_PORT...")
            val socket = server.accept()
            socket.keepAlive = true
            socket.soTimeout = (READ_TIMEOUT_SECONDS * 1000).toInt()

            Timber.d("Conexión entrante aceptada de ${socket.inetAddress.hostAddress}")
            TcpManagedSocket(socket)
        }
    }

    /**
     * Socket gestionado sobre TCP.
     *
     * Encapsula un Socket Java estándar y provee una API basada en corrutinas.
     * La recepción se implementa como callbackFlow que emite ByteArrays a
     * medida que se leen del InputStream.
     */
    private class TcpManagedSocket(
        private val socket: Socket
    ) : SocketManager.ManagedSocket {

        private val inputStream: InputStream = socket.getInputStream()
        private val outputStream: OutputStream = socket.getOutputStream()

        override val isConnected: Boolean
            get() = socket.isConnected && !socket.isClosed

        override suspend fun send(data: ByteArray) {
            withContext(Dispatchers.IO) {
                try {
                    // Enviar primero la longitud del mensaje (int, 4 bytes) para
                    // que el receptor sepa cuántos bytes leer.
                    val lengthPrefix = intToByteArray(data.size)
                    outputStream.write(lengthPrefix)
                    outputStream.write(data)
                    outputStream.flush()
                } catch (e: Exception) {
                    Timber.e(e, "Error al enviar datos")
                    throw e
                }
            }
        }

        override fun receiveFlow(): Flow<ByteArray> = callbackFlow {
            withContext(Dispatchers.IO) {
                try {
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (isConnected) {
                        // Leer prefijo de longitud (4 bytes)
                        val lengthBytes = ByteArray(4)
                        readFully(inputStream, lengthBytes)
                        val messageLength = byteArrayToInt(lengthBytes)

                        // Leer el mensaje completo
                        val messageData = ByteArray(messageLength)
                        readFully(inputStream, messageData)

                        trySend(messageData)
                    }
                } catch (e: Exception) {
                    Timber.d(e, "Flujo de recepción terminado")
                }
            }

            awaitClose {
                close()
            }
        }

        override suspend fun close() {
            withContext(Dispatchers.IO) {
                try {
                    if (!socket.isClosed) {
                        socket.close()
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al cerrar socket")
                }
            }
        }

        /**
         * Lee exactamente [data.size] bytes del [inputStream].
         *
         * POR QUÉ readFully: InputStream.read() no garantiza leer todos los
         * bytes solicitados en una sola llamada. Este helper itera hasta
         * completar la lectura, manejando correctamente el caso de bytes
         * fragmentados en la red.
         */
        private fun readFully(inputStream: InputStream, data: ByteArray) {
            var offset = 0
            while (offset < data.size) {
                val bytesRead = inputStream.read(data, offset, data.size - offset)
                if (bytesRead == -1) throw java.io.EOFException("Conexión cerrada por el peer")
                offset += bytesRead
            }
        }

        /** Convierte Int a ByteArray (big-endian, 4 bytes). */
        private fun intToByteArray(value: Int): ByteArray = byteArrayOf(
            (value shr 24).toByte(),
            (value shr 16).toByte(),
            (value shr 8).toByte(),
            value.toByte()
        )

        /** Convierte ByteArray (4 bytes, big-endian) a Int. */
        private fun byteArrayToInt(bytes: ByteArray): Int =
            (bytes[0].toInt() and 0xFF shl 24) or
                (bytes[1].toInt() and 0xFF shl 16) or
                (bytes[2].toInt() and 0xFF shl 8) or
                (bytes[3].toInt() and 0xFF)
    }
}
