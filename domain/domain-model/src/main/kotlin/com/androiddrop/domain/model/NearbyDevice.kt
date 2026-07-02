package com.androiddrop.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class NearbyDevice(
    val deviceId: String,
    val deviceName: String,
    val deviceType: DeviceType,
    val connectionInfo: ConnectionInfo,
    val signalStrength: Int
)

@Serializable
enum class DeviceType {
    PHONE, TABLET, TV, WEARABLE, DESKTOP
}

@Serializable
data class ConnectionInfo(
    val transportType: TransportType,
    val address: String,
    val port: Int
)

@Serializable
enum class TransportType {
    BLE, WIFI_DIRECT, NEARBY, TCP
}
