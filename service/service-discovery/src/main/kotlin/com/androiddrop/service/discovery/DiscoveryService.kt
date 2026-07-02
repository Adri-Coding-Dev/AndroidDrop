package com.androiddrop.service.discovery

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.androiddrop.data.ble.BleAdvertiser
import com.androiddrop.data.ble.BleScanner
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

sealed interface DiscoveryState {
    data object Scanning : DiscoveryState
    data object Idle : DiscoveryState
    data class Error(val message: String) : DiscoveryState
}

@AndroidEntryPoint
class DiscoveryService : Service() {

    @Inject lateinit var notificationManager: DiscoveryNotificationManager
    @Inject lateinit var bleAdvertiser: BleAdvertiser
    @Inject lateinit var bleScanner: BleScanner
    @Inject @ApplicationContext lateinit var appContext: Context

    private val binder = DiscoveryBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow<DiscoveryState>(DiscoveryState.Idle)
    val state: StateFlow<DiscoveryState> = _state.asStateFlow()

    private val _discoveredDeviceCount = MutableStateFlow(0)
    val discoveredDeviceCount: StateFlow<Int> = _discoveredDeviceCount.asStateFlow()

    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        private val SERVICE_UUID: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
        private const val DEVICE_NAME = "AndroidDrop"
    }

    inner class DiscoveryBinder : Binder() {
        fun getService(): DiscoveryService = this@DiscoveryService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        Timber.d("DiscoveryService: onCreate")
        notificationManager.createNotificationChannel()
        adquirirWakeLock()
        iniciarDescubrimiento()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("DiscoveryService: onStartCommand")
        val notification = notificationManager.buildNotification(_discoveredDeviceCount.value)
        startForeground(
            DiscoveryNotificationManager.NOTIFICATION_ID,
            notification
        )
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("DiscoveryService: onDestroy")
        detenerDescubrimiento()
        liberarWakeLock()
        serviceScope.cancel()
    }

    private fun adquirirWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AndroidDrop:DiscoveryWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L)
        }
    }

    private fun liberarWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }

    private fun iniciarDescubrimiento() {
        serviceScope.launch {
            _state.value = DiscoveryState.Scanning
            try {
                val bluetoothManager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                val adapter = bluetoothManager?.adapter
                if (adapter == null || !adapter.isEnabled) {
                    Timber.w("DiscoveryService: Bluetooth no disponible")
                    _state.value = DiscoveryState.Error("Bluetooth no disponible")
                    return@launch
                }

                // Iniciar advertising BLE
                bleAdvertiser.startAdvertising(DEVICE_NAME, SERVICE_UUID) { success ->
                    if (success) {
                        Timber.d("DiscoveryService: advertising BLE iniciado")
                    } else {
                        Timber.w("DiscoveryService: advertising BLE falló")
                    }
                }

                Timber.d("DiscoveryService: descubrimiento BLE iniciado")
                _state.value = DiscoveryState.Scanning
            } catch (e: Exception) {
                Timber.e(e, "DiscoveryService: error al iniciar descubrimiento")
                _state.value = DiscoveryState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    private fun detenerDescubrimiento() {
        _state.value = DiscoveryState.Idle
        bleAdvertiser.stopAdvertising()
        Timber.d("DiscoveryService: descubrimiento detenido")
    }

    fun actualizarDispositivosEncontrados(count: Int) {
        _discoveredDeviceCount.value = count
        val notification = notificationManager.buildNotification(count)
        val notificationManagerCompat =
            getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManagerCompat.notify(
            DiscoveryNotificationManager.NOTIFICATION_ID,
            notification
        )
    }
}
