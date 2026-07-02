package com.androiddrop.core.network

import com.androiddrop.core.common.Constants
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * Serializador de paquetes de red del protocolo AndroidDrop.
 *
 * POR QUÉ Protocol Buffers vs JSON: JSON es verboso (cada paquete incluye
 * nombres de campos), lo que añade overhead innecesario a la transferencia.
 * Protobuf es binario, compacto y rápido de serializar/deserializar. Para
 * transferencias de archivos grandes donde cada byte cuenta, Protobuf es
 * la opción óptima.
 *
 * POR QUÉ un serializador centralizado vs disperso: Tener un único punto
 * de serialización garantiza que todos los paquetes sigan el mismo formato
 * y versión de protocolo. Cualquier cambio en el formato se hace en un
 * solo lugar.
 */
object PacketSerializer {

    /**
     * Serializa un [NetworkPacket] a ByteArray para transmisión.
     *
     * @param packet El paquete a serializar.
     * @return ByteArray listo para enviar por el socket.
     */
    fun serialize(packet: NetworkPacket): ByteArray {
        return ProtoBuf.encodeToByteArray(packet)
    }

    /**
     * Deserializa un ByteArray recibido a [NetworkPacket].
     *
     * @param data Los bytes recibidos del socket.
     * @return El paquete deserializado.
     */
    fun deserialize(data: ByteArray): NetworkPacket {
        return ProtoBuf.decodeFromByteArray(data)
    }
}

/**
 * Paquete de red del protocolo AndroidDrop.
 *
 * POR QUÉ esta estructura: Cada paquete contiene metadatos esenciales para
 * el protocolo: identidad del emisor, sesión, secuencia para reordenamiento,
 * timestamp para detección de latencia, y firma para integridad. El payload
 * contiene los datos específicos según el tipo de paquete.
 *
 * @property version Versión del protocolo (para compatibilidad hacia atrás).
 * @property type Tipo de paquete (determina cómo interpretar el payload).
 * @property deviceId ID del dispositivo emisor.
 * @property sessionId ID de la sesión de transferencia.
 * @property sequenceNumber Número de secuencia para reordenamiento de chunks.
 * @property timestamp Timestamp Unix (milisegundos) de emisión.
 * @property payload Datos del paquete (interpretación según [type]).
 * @property signature HMAC-SHA256 del resto del paquete para verificación.
 */
@Serializable
data class NetworkPacket(
    val version: Int = Constants.PROTOCOL_VERSION,
    val type: PacketType,
    val deviceId: String,
    val sessionId: String,
    val sequenceNumber: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val payload: ByteArray = ByteArray(0),
    val signature: ByteArray = ByteArray(0)
)

/**
 * Tipos de paquete del protocolo AndroidDrop.
 *
 * POR QUÉ estos tipos: Cada etapa del protocolo de transferencia requiere
 * un tipo de mensaje específico. La clasificación permite al receptor
 * enrutar cada paquete al handler correcto sin inspeccionar el payload.
 */
enum class PacketType {
    /** Descubrimiento inicial de peers en la red. */
    DISCOVERY,

    /** Establecimiento de conexión (inicio del handshake). */
    HANDSHAKE,

    /** Desafío de autenticación (reto criptográfico). */
    AUTH_CHALLENGE,

    /** Respuesta al desafío de autenticación. */
    AUTH_RESPONSE,

    /** Metadatos del archivo a transferir (nombre, tamaño, tipo). */
    FILE_METADATA,

    /** Fragmento (chunk) de datos del archivo. */
    CHUNK_DATA,

    /** Confirmación de recepción de un chunk. */
    CHUNK_ACK,

    /** Sincronización de estado de la sesión. */
    STATE_SYNC,

    /** Sincronización de progreso de transferencia. */
    PROGRESS_SYNC,

    /** Mantenimiento de conexión (keep alive). */
    KEEP_ALIVE,

    /** Señal de transferencia completada exitosamente. */
    TRANSFER_COMPLETE,

    /** Señal de cancelación de transferencia. */
    TRANSFER_CANCEL,

    /** Señal de error en la transferencia. */
    ERROR
}
