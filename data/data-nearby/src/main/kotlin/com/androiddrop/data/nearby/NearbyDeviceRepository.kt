package com.androiddrop.data.nearby

import android.content.Context
import com.androiddrop.domain.model.ConnectionInfo
import com.androiddrop.domain.model.DeviceType
import com.androiddrop.domain.model.NearbyDevice
import com.androiddrop.domain.model.TransportType
import com.androiddrop.domain.repository.DeviceRepository
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionInfo as NearbyConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

class NearbyDeviceRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : DeviceRepository {

    private val connectedDeviceFlow = MutableStateFlow<NearbyDevice?>(null)

    companion object {
        private const val SERVICE_ID = "com.androiddrop.nearby"
    }

    override fun discoverDevices(): Flow<NearbyDevice> = callbackFlow {
        Timber.d("Iniciando descubrimiento Nearby Connections")

        val connectionsClient = Nearby.getConnectionsClient(context)

        val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(
                endpointId: String,
                info: DiscoveredEndpointInfo
            ) {
                Timber.d("Endpoint encontrado: $endpointId (${info.endpointName})")

                val device = NearbyDevice(
                    deviceId = endpointId,
                    deviceName = info.endpointName,
                    deviceType = DeviceType.PHONE,
                    connectionInfo = ConnectionInfo(
                        transportType = TransportType.NEARBY,
                        address = endpointId,
                        port = 0
                    ),
                    signalStrength = 100
                )
                trySend(device)
            }

            override fun onEndpointLost(endpointId: String) {
                Timber.d("Endpoint perdido: $endpointId")
            }
        }

        val strategy = Strategy.P2P_STAR
        val discoveryOptions = DiscoveryOptions.Builder()
            .setStrategy(strategy)
            .build()

        connectionsClient.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            discoveryOptions
        ).addOnSuccessListener {
            Timber.d("Descubrimiento Nearby iniciado")
        }.addOnFailureListener { e ->
            Timber.e(e, "Fallo al iniciar descubrimiento Nearby")
        }

        awaitClose {
            connectionsClient.stopDiscovery()
            Timber.d("Descubrimiento Nearby detenido")
        }
    }

    override suspend fun connectToDevice(device: NearbyDevice): Boolean {
        return try {
            val connectionsClient = Nearby.getConnectionsClient(context)

            val result = suspendCancellableCoroutine<Boolean> { cont ->
                val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
                    override fun onConnectionInitiated(endpointId: String, info: NearbyConnectionInfo) {
                        Timber.d("Conexión iniciada con $endpointId")
                        connectionsClient.acceptConnection(endpointId, object : PayloadCallback() {
                            override fun onPayloadReceived(endpointId: String, payload: Payload) {
                                Timber.d("Payload recibido de $endpointId")
                            }

                            override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
                                // Manejar progreso
                            }
                        })
                    }

                    override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
                        when (resolution.status.statusCode) {
                            com.google.android.gms.common.api.CommonStatusCodes.SUCCESS -> {
                                Timber.d("Conexión establecida con $endpointId")
                                connectedDeviceFlow.value = device
                                if (cont.isActive) cont.resume(true)
                            }
                            else -> {
                                Timber.w("Fallo de conexión con $endpointId: ${resolution.status.statusCode}")
                                if (cont.isActive) cont.resume(false)
                            }
                        }
                    }

                    override fun onDisconnected(endpointId: String) {
                        Timber.d("Desconectado de $endpointId")
                        connectedDeviceFlow.value = null
                    }
                }

                connectionsClient.requestConnection(
                    device.deviceName,
                    device.deviceId,
                    connectionLifecycleCallback
                ).addOnSuccessListener {
                    Timber.d("Solicitud de conexión enviada a ${device.deviceName}")
                }.addOnFailureListener { e ->
                    Timber.e(e, "Fallo al solicitar conexión con ${device.deviceName}")
                    if (cont.isActive) cont.resume(false)
                }
            }

            result
        } catch (e: Exception) {
            Timber.e(e, "Error al conectar con ${device.deviceName}")
            false
        }
    }

    override suspend fun authenticateDevice(
        device: NearbyDevice
    ): Result<Boolean> {
        Timber.d("Autenticación delegada a BLEDeviceRepository para ${device.deviceName}")
        return Result.success(true)
    }

    override suspend fun disconnectDevice(deviceId: String) {
        try {
            val connectionsClient = Nearby.getConnectionsClient(context)
            connectionsClient.disconnectFromEndpoint(deviceId)
            Timber.d("Desconectado de $deviceId")
            connectedDeviceFlow.value = null
        } catch (e: Exception) {
            Timber.e(e, "Error al desconectar de $deviceId")
        }
    }

    override fun getConnectedDevice(): Flow<NearbyDevice?> = connectedDeviceFlow
}
