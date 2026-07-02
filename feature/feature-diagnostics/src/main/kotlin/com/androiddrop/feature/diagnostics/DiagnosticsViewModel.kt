package com.androiddrop.feature.diagnostics

import android.app.Application
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.bluetooth.BluetoothAdapter
import android.net.wifi.p2p.WifiP2pManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class DiagnosticsUiState(
    val logs: List<LogEntry> = emptyList(),
    val deviceInfo: DeviceInfo = DeviceInfo(),
    val isMonitoring: Boolean = false,
    val filterLevel: LogLevel? = null
)

data class LogEntry(
    val timestamp: Long,
    val level: LogLevel,
    val tag: String,
    val message: String
) {
    val formattedTimestamp: String
        get() = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))
}

enum class LogLevel {
    DEBUG, INFO, WARNING, ERROR
}

data class DeviceInfo(
    val deviceModel: String = "${Build.MANUFACTURER} ${Build.MODEL}",
    val androidVersion: String = Build.VERSION.RELEASE,
    val apiLevel: Int = Build.VERSION.SDK_INT,
    val availableStorage: Long = 0L,
    val bleSupported: Boolean = false,
    val wifiDirectSupported: Boolean = false,
    val screenRefreshRate: Float = 60f
) {
    val availableStorageFormatted: String
        get() {
            val gb = availableStorage.toDouble() / (1024 * 1024 * 1024)
            return String.format("%.2f GB", gb)
        }
}

sealed interface DiagnosticsIntent {
    data object StartLogCapture : DiagnosticsIntent
    data object StopLogCapture : DiagnosticsIntent
    data object ClearLogs : DiagnosticsIntent
    data object RefreshDeviceInfo : DiagnosticsIntent
    data class SetFilterLevel(val level: LogLevel?) : DiagnosticsIntent
}

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : AndroidViewModel(context as Application) {

    private val _uiState = MutableStateFlow(DiagnosticsUiState())
    val uiState: StateFlow<DiagnosticsUiState> = _uiState.asStateFlow()

    private val _logEntries = MutableSharedFlow<LogEntry>(replay = 0)
    val logEntries = _logEntries.asSharedFlow()

    private var timberTree: Timber.Tree? = null

    init {
        refreshDeviceInfo()
    }

    fun onIntent(intent: DiagnosticsIntent) {
        when (intent) {
            DiagnosticsIntent.StartLogCapture -> startLogCapture()
            DiagnosticsIntent.StopLogCapture -> stopLogCapture()
            DiagnosticsIntent.ClearLogs -> clearLogs()
            DiagnosticsIntent.RefreshDeviceInfo -> refreshDeviceInfo()
            is DiagnosticsIntent.SetFilterLevel -> {
                _uiState.value = _uiState.value.copy(filterLevel = intent.level)
            }
        }
    }

    companion object {
        private const val MAX_LOG_ENTRIES = 1000
    }

    private fun startLogCapture() {
        if (_uiState.value.isMonitoring) return

        val tree = object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                val logLevel = when (priority) {
                    android.util.Log.DEBUG -> LogLevel.DEBUG
                    android.util.Log.INFO -> LogLevel.INFO
                    android.util.Log.WARN -> LogLevel.WARNING
                    android.util.Log.ERROR -> LogLevel.ERROR
                    else -> LogLevel.DEBUG
                }
                val entry = LogEntry(
                    timestamp = System.currentTimeMillis(),
                    level = logLevel,
                    tag = tag ?: "AndroidDrop",
                    message = message
                )
                viewModelScope.launch {
                    _logEntries.emit(entry)
                    val currentLogs = _uiState.value.logs.toMutableList()
                    currentLogs.add(entry)
                    // Limitar tamaño para evitar OOM
                    if (currentLogs.size > MAX_LOG_ENTRIES) {
                        currentLogs.removeAt(0)
                    }
                    _uiState.value = _uiState.value.copy(logs = currentLogs)
                }
            }
        }

        Timber.plant(tree)
        timberTree = tree

        // Generar un log de prueba para confirmar que la captura funciona
        Timber.d("Captura de logs iniciada")
        Timber.i("Diagnóstico de AndroidDrop - monitoreo activo")
        Timber.i("Dispositivo: ${_uiState.value.deviceInfo.deviceModel}")
        Timber.i("Android: ${_uiState.value.deviceInfo.androidVersion} (API ${_uiState.value.deviceInfo.apiLevel})")

        _uiState.value = _uiState.value.copy(isMonitoring = true)
    }

    private fun stopLogCapture() {
        timberTree?.let { Timber.uproot(it) }
        timberTree = null
        _uiState.value = _uiState.value.copy(isMonitoring = false)
    }

    private fun clearLogs() {
        _uiState.value = _uiState.value.copy(logs = emptyList())
    }

    fun exportLogs(): String {
        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sb.appendLine("=== AndroidDrop Diagnostics Log ===")
        sb.appendLine("Exportado: ${dateFormat.format(Date())}")
        sb.appendLine("Dispositivo: ${_uiState.value.deviceInfo.deviceModel}")
        sb.appendLine("Android: ${_uiState.value.deviceInfo.androidVersion} (API ${_uiState.value.deviceInfo.apiLevel})")
        sb.appendLine("======================================")
        sb.appendLine()
        _uiState.value.logs.forEach { entry ->
            sb.appendLine("[${entry.formattedTimestamp}] [${entry.level.name}] [${entry.tag}] ${entry.message}")
        }
        return sb.toString()
    }

    private fun refreshDeviceInfo() {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        val refreshRate = displayManager?.displays?.firstOrNull()?.refreshRate ?: 60f

        val statFs = StatFs(Environment.getDataDirectory().absolutePath)
        val availableBytes = statFs.availableBlocksLong * statFs.blockSizeLong

        val bleAdapter = BluetoothAdapter.getDefaultAdapter()
        val bleSupported = bleAdapter != null

        val wifiDirectSupported = try {
            context.packageManager.hasSystemFeature(
                android.content.pm.PackageManager.FEATURE_WIFI_DIRECT
            )
        } catch (_: Exception) {
            false
        }

        _uiState.value = _uiState.value.copy(
            deviceInfo = DeviceInfo(
                availableStorage = availableBytes,
                bleSupported = bleSupported,
                wifiDirectSupported = wifiDirectSupported,
                screenRefreshRate = refreshRate
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        stopLogCapture()
    }
}
