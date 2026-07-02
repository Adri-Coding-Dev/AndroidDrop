package com.androiddrop.data.wifidirect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

class WiFiDirectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var wifiP2pManager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null
    private var receiver: BroadcastReceiver? = null
    private var isInitialized = false

    private val _connectionInfo = MutableStateFlow<WifiP2pInfo?>(null)
    val connectionInfo = _connectionInfo.asStateFlow()

    private val _thisDevice = MutableStateFlow<WifiP2pDevice?>(null)
    val thisDevice = _thisDevice.asStateFlow()

    private val discoveredPeers = mutableListOf<WifiP2pDevice>()

    fun initialize(): Boolean {
        return try {
            wifiP2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
            if (wifiP2pManager == null) {
                Timber.w("WiFi P2P no soportado en este dispositivo")
                return false
            }

            channel = wifiP2pManager?.initialize(context, context.mainLooper, null)
            if (channel == null) {
                Timber.w("Fallo al inicializar canal WiFi P2P")
                return false
            }

            registerReceiver()
            isInitialized = true
            Timber.d("WiFi Direct inicializado exitosamente")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error al inicializar WiFi Direct")
            false
        }
    }

    fun discoverPeers(): Flow<WifiP2pDevice> = callbackFlow {
        if (!isInitialized) {
            Timber.w("WiFi Direct no inicializado")
            channel.close()
            return@callbackFlow
        }

        wifiP2pManager?.discoverPeers(this@WiFiDirectManager.channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Timber.d("Descubrimiento de pares iniciado")
            }

            override fun onFailure(reason: Int) {
                Timber.w("Fallo al iniciar descubrimiento de pares: $reason")
            }
        })

        val peerListListener = WifiP2pManager.PeerListListener { peerList ->
            val newPeers = peerList.deviceList.filter { device ->
                !discoveredPeers.any { it.deviceAddress == device.deviceAddress }
            }
            discoveredPeers.addAll(newPeers)
            newPeers.forEach { trySend(it) }
        }

        val originalReceiver = receiver
        if (originalReceiver is WiFiDirectBroadcastReceiver) {
            originalReceiver.setPeerListListener(peerListListener)
        }

        awaitClose {
            stopDiscovery()
        }
    }

    suspend fun connectToPeer(deviceAddress: String): Result<WifiP2pInfo> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                try {
                    val config = WifiP2pConfig().apply {
                        this.deviceAddress = deviceAddress
                        groupOwnerIntent = 15
                    }

                    wifiP2pManager?.connect(this@WiFiDirectManager.channel, config, object : WifiP2pManager.ActionListener {
                        override fun onSuccess() {
                            Timber.d("Solicitud de conexión enviada a $deviceAddress")
                            _connectionInfo.value?.let { info ->
                                continuation.resume(Result.success(info))
                            }
                        }

                        override fun onFailure(reason: Int) {
                            Timber.w("Fallo al conectar con $deviceAddress: $reason")
                            continuation.resume(
                                Result.failure(Exception("Fallo de conexión WiFi Direct: $reason"))
                            )
                        }
                    })
                } catch (e: Exception) {
                    continuation.resume(Result.failure(e))
                }
            }
        }
    }

    suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            try {
                wifiP2pManager?.removeGroup(channel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Timber.d("Grupo WiFi Direct eliminado")
                    }

                    override fun onFailure(reason: Int) {
                        Timber.w("Fallo al eliminar grupo WiFi Direct: $reason")
                    }
                })
                _connectionInfo.value = null
                discoveredPeers.clear()
            } catch (e: Exception) {
                Timber.e(e, "Error al desconectar WiFi Direct")
            }
        }
    }

    fun getConnectionInfo(): Flow<WifiP2pInfo?> {
        return connectionInfo
    }

    fun cleanup() {
        try {
            stopDiscovery()
            if (isInitialized) {
                context.unregisterReceiver(receiver)
            }
            receiver = null
            channel = null
            wifiP2pManager = null
            isInitialized = false
            discoveredPeers.clear()
            Timber.d("WiFi Direct liberado")
        } catch (e: Exception) {
            Timber.e(e, "Error al liberar WiFi Direct")
        }
    }

    private fun stopDiscovery() {
        try {
            wifiP2pManager?.stopPeerDiscovery(channel, null)
        } catch (e: Exception) {
            Timber.e(e, "Error al detener descubrimiento")
        }
    }

    private fun registerReceiver() {
        val filter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        }

        receiver = WiFiDirectBroadcastReceiver(
            wifiP2pManager!!,
            channel!!,
            connectionInfoCallback = { info -> _connectionInfo.value = info },
            thisDeviceCallback = { device -> _thisDevice.value = device }
        )

        context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
    }

    private class WiFiDirectBroadcastReceiver(
        private val manager: WifiP2pManager,
        private val channel: WifiP2pManager.Channel,
        private val connectionInfoCallback: (WifiP2pInfo?) -> Unit,
        private val thisDeviceCallback: (WifiP2pDevice?) -> Unit
    ) : BroadcastReceiver() {

        private var peerListListener: WifiP2pManager.PeerListListener? = null

        fun setPeerListListener(listener: WifiP2pManager.PeerListListener) {
            peerListListener = listener
        }

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    manager.requestPeers(channel) { peers ->
                        peerListListener?.onPeersAvailable(peers)
                    }
                }

                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    val networkInfo = intent.getParcelableExtra<NetworkInfo>(
                        WifiP2pManager.EXTRA_NETWORK_INFO
                    )
                    if (networkInfo?.isConnected == true) {
                        manager.requestConnectionInfo(channel) { info ->
                            connectionInfoCallback(info)
                        }
                    } else {
                        connectionInfoCallback(null)
                    }
                }

                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    val device = intent.getParcelableExtra<WifiP2pDevice>(
                        WifiP2pManager.EXTRA_WIFI_P2P_DEVICE
                    )
                    thisDeviceCallback(device)
                }
            }
        }
    }
}
