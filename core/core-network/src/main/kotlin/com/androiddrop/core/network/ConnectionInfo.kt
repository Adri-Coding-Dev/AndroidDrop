package com.androiddrop.core.network

/**
 * Información de conexión de un peer descubierto en la red.
 *
 * POR QUÉ esta estructura: Cuando un dispositivo AndroidDrop descubre a otro,
 * necesita suficiente información para establecer una conexión TCP/IP.
 * ConnectionInfo encapsula todos los datos necesarios: identidad del peer,
 * dirección de red, y tipo de transporte subyacente.
 *
 * @property deviceId Identificador único del dispositivo (UUID persistente).
 * @property deviceName Nombre amigable del dispositivo (visible al usuario).
 * @property ipAddress Dirección IP del peer (IPv4 o IPv6).
 * @property port Puerto TCP donde el peer escucha conexiones.
 * @property transportType Tipo de transporte que descubrió al peer.
 */
data class ConnectionInfo(
    val deviceId: String,
    val deviceName: String,
    val ipAddress: String,
    val port: Int,
    val transportType: TransportType
)

/**
 * Tipo de transporte utilizado para la conexión.
 *
 * POR QUÉ múltiples transportes: AndroidDrop soporta múltiples tecnologías
 * de descubrimiento y conexión (BLE, WiFi Direct, Nearby Connections, TCP
 * directo). Cada una tiene ventajas y casos de uso diferentes. El tipo de
 * transporte permite al sistema elegir la estrategia óptima para cada par.
 *
 * @property BLE Bluetooth Low Energy: bajo consumo, corto alcance, ideal para descubrimiento.
 * @property WIFI_DIRECT WiFi Direct: peer-to-peer sin infraestructura, alto throughput.
 * @property NEARBY Google Nearby Connections: abstracción de múltiples transportes.
 * @property TCP TCP directo: conexión tradicional sobre WiFi/LAN/Ethernet.
 */
enum class TransportType {
    BLE,
    WIFI_DIRECT,
    NEARBY,
    TCP
}
