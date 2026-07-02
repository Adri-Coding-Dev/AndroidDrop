package com.androiddrop.feature.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddrop.domain.model.NearbyDevice
import com.androiddrop.domain.usecase.ConnectToDeviceUseCase
import com.androiddrop.domain.usecase.DiscoverDevicesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed interface DiscoveryUiState {
    data object Idle : DiscoveryUiState
    data class Scanning(val devices: List<NearbyDevice>) : DiscoveryUiState
    data class DevicesFound(val devices: List<NearbyDevice>) : DiscoveryUiState
    data class Connecting(val device: NearbyDevice) : DiscoveryUiState
    data class Connected(val device: NearbyDevice) : DiscoveryUiState
    data class Error(val message: String) : DiscoveryUiState
}

sealed interface DiscoveryIntent {
    data object StartScanning : DiscoveryIntent
    data object StopScanning : DiscoveryIntent
    data class SelectDevice(val device: NearbyDevice) : DiscoveryIntent
    data object Disconnect : DiscoveryIntent
}

sealed interface DiscoverySideEffect {
    data class DeviceConnected(val device: NearbyDevice) : DiscoverySideEffect
    data class Error(val message: String) : DiscoverySideEffect
}

@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val discoverDevicesUseCase: DiscoverDevicesUseCase,
    private val connectToDeviceUseCase: ConnectToDeviceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiscoveryUiState>(DiscoveryUiState.Idle)
    val uiState: StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    private var scanningJob: Job? = null

    fun onIntent(intent: DiscoveryIntent) {
        when (intent) {
            is DiscoveryIntent.StartScanning -> startScanning()
            is DiscoveryIntent.StopScanning -> stopScanning()
            is DiscoveryIntent.SelectDevice -> selectDevice(intent.device)
            is DiscoveryIntent.Disconnect -> disconnect()
        }
    }

    private fun startScanning() {
        _uiState.value = DiscoveryUiState.Scanning(devices = emptyList())

        scanningJob = viewModelScope.launch {
            discoverDevicesUseCase().collect { device ->
                val current = when (val state = _uiState.value) {
                    is DiscoveryUiState.Scanning -> state.devices
                    is DiscoveryUiState.DevicesFound -> state.devices
                    else -> return@collect
                }

                val updated = if (current.none { it.deviceId == device.deviceId }) {
                    current + device
                } else {
                    current.map {
                        if (it.deviceId == device.deviceId && it.signalStrength < device.signalStrength) {
                            device
                        } else {
                            it
                        }
                    }
                }

                val sorted = updated.sortedByDescending { it.signalStrength }

                _uiState.value = if (sorted.isEmpty()) {
                    DiscoveryUiState.Scanning(devices = sorted)
                } else {
                    DiscoveryUiState.DevicesFound(devices = sorted)
                }

                Timber.d("Dispositivos descubiertos: ${sorted.size}")
            }
        }
    }

    private fun stopScanning() {
        scanningJob?.cancel()
        scanningJob = null
        val current = _uiState.value
        if (current is DiscoveryUiState.Scanning || current is DiscoveryUiState.DevicesFound) {
            _uiState.value = DiscoveryUiState.Idle
        }
    }

    private fun selectDevice(device: NearbyDevice) {
        _uiState.value = DiscoveryUiState.Connecting(device = device)

        viewModelScope.launch {
            val result = connectToDeviceUseCase(device)
            if (result.isSuccess) {
                _uiState.value = DiscoveryUiState.Connected(device = device)
                Timber.d("Conectado a: ${device.deviceName}")
            } else {
                val message = result.exceptionOrNull()?.message ?: "Error al conectar"
                _uiState.value = DiscoveryUiState.Error(message = message)
                Timber.e("Error de conexión: $message")
            }
        }
    }

    private fun disconnect() {
        scanningJob?.cancel()
        scanningJob = null
        _uiState.value = DiscoveryUiState.Idle
        Timber.d("Desconectado")
    }

    override fun onCleared() {
        super.onCleared()
        scanningJob?.cancel()
    }
}
