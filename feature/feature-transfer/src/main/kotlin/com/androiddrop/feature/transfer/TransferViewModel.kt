package com.androiddrop.feature.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddrop.core.common.Vector3
import com.androiddrop.domain.model.FileNode
import com.androiddrop.domain.model.NearbyDevice
import com.androiddrop.domain.model.SphereState
import com.androiddrop.domain.model.TransferError
import com.androiddrop.domain.model.TransferPhase
import com.androiddrop.domain.model.TransferProgress
import com.androiddrop.domain.usecase.CancelTransferUseCase
import com.androiddrop.domain.usecase.ConnectToDeviceUseCase
import com.androiddrop.domain.usecase.DiscoverDevicesUseCase
import com.androiddrop.domain.usecase.StartTransferUseCase
import com.androiddrop.domain.usecase.SyncAnimationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed interface TransferIntent {
    data class SelectFile(val file: FileNode) : TransferIntent
    data object StartDiscovery : TransferIntent
    data class ConnectToDevice(val device: NearbyDevice) : TransferIntent
    data class UpdateSpherePosition(val position: Vector3) : TransferIntent
    data class LaunchSphere(val velocity: Vector3) : TransferIntent
    data object CancelTransfer : TransferIntent
    data object ResetTransfer : TransferIntent
}

sealed interface SideEffect {
    data class ShowSuccess(val sessionId: String) : SideEffect
    data class ShowError(val message: String) : SideEffect
    data object NavigateToDiagnostics : SideEffect
}

data class TransferUiState(
    val phase: TransferPhase = TransferPhase.IDLE,
    val selectedFile: FileNode? = null,
    val discoveredDevices: List<NearbyDevice> = emptyList(),
    val pairedDevice: NearbyDevice? = null,
    val transferProgress: Float = 0f,
    val sphereState: SphereState = SphereState.Idle,
    val sphereEnergy: Float = 1f,
    val sphereScale: Float = 1f,
    val spherePosition: Vector3 = Vector3(0f, 0f, 0f),
    val error: TransferError? = null,
    val sideEffect: SideEffect? = null
)

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val startTransferUseCase: StartTransferUseCase,
    private val cancelTransferUseCase: CancelTransferUseCase,
    private val discoverDevicesUseCase: DiscoverDevicesUseCase,
    private val connectToDeviceUseCase: ConnectToDeviceUseCase,
    private val syncAnimationUseCase: SyncAnimationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    private val _sideEffects = MutableSharedFlow<SideEffect>()
    val sideEffects: SharedFlow<SideEffect> = _sideEffects.asSharedFlow()

    private var transferJob: Job? = null
    private var discoveryJob: Job? = null

    fun onIntent(intent: TransferIntent) {
        when (intent) {
            is TransferIntent.SelectFile -> handleSelectFile(intent.file)
            is TransferIntent.StartDiscovery -> handleStartDiscovery()
            is TransferIntent.ConnectToDevice -> handleConnectToDevice(intent.device)
            is TransferIntent.UpdateSpherePosition -> handleUpdateSpherePosition(intent.position)
            is TransferIntent.LaunchSphere -> handleLaunchSphere(intent.velocity)
            is TransferIntent.CancelTransfer -> handleCancelTransfer()
            is TransferIntent.ResetTransfer -> handleResetTransfer()
        }
    }

    private fun handleSelectFile(file: FileNode) {
        _uiState.value = _uiState.value.copy(
            phase = TransferPhase.FILE_SELECTED,
            selectedFile = file,
            sphereState = SphereState.Found,
            sphereEnergy = 1f
        )
        Timber.d("Archivo seleccionado para transferencia: ${file.name}")
    }

    private fun handleStartDiscovery() {
        _uiState.value = _uiState.value.copy(
            phase = TransferPhase.DISCOVERING,
            discoveredDevices = emptyList()
        )

        discoveryJob = viewModelScope.launch {
            discoverDevicesUseCase().collect { device ->
                val current = _uiState.value.discoveredDevices
                if (current.none { it.deviceId == device.deviceId }) {
                    _uiState.value = _uiState.value.copy(
                        discoveredDevices = current + device,
                        phase = TransferPhase.DEVICE_FOUND
                    )
                    Timber.d("Dispositivo descubierto: ${device.deviceName}")
                }
            }
        }
    }

    private fun handleConnectToDevice(device: NearbyDevice) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                pairedDevice = device,
                sphereState = SphereState.Held(
                    position = Vector3.zero,
                    velocity = Vector3.zero
                ),
                phase = TransferPhase.SPHERE_HELD
            )

            val result = connectToDeviceUseCase(device)
            if (result.isSuccess) {
                Timber.d("Conectado a: ${device.deviceName}")
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Error de conexión"
                _uiState.value = _uiState.value.copy(
                    error = TransferError(
                        code = com.androiddrop.domain.model.ErrorCode.NETWORK_ERROR,
                        message = errorMessage,
                        recoverable = true
                    ),
                    phase = TransferPhase.ERROR
                )
                emitSideEffect(SideEffect.ShowError(errorMessage))
            }
        }
    }

    private fun handleUpdateSpherePosition(position: Vector3) {
        _uiState.value = _uiState.value.copy(
            spherePosition = position,
            sphereState = SphereState.Held(
                position = position,
                velocity = Vector3.zero
            )
        )
    }

    private fun handleLaunchSphere(velocity: Vector3) {
        val file = _uiState.value.selectedFile ?: return
        val device = _uiState.value.pairedDevice ?: return

        _uiState.value = _uiState.value.copy(
            phase = TransferPhase.SPHERE_LAUNCHED,
            sphereState = SphereState.Launched(
                velocity = velocity,
                targetPosition = Vector3(0f, 0f, -1f)
            )
        )

        transferJob = viewModelScope.launch {
            animateSphereEntry()
            startFileTransfer(file, device)
        }
    }

    private suspend fun animateSphereEntry() {
        _uiState.value = _uiState.value.copy(
            phase = TransferPhase.ENTERING_PORTAL,
            sphereState = SphereState.Entering
        )
        delay(800)
        _uiState.value = _uiState.value.copy(
            phase = TransferPhase.TRANSFERRING,
            sphereState = SphereState.Decaying(progress = 0f)
        )
    }

    private suspend fun startFileTransfer(file: FileNode, device: NearbyDevice) {
        startTransferUseCase(file, device).collect { progress ->
            when (progress) {
                is TransferProgress.Idle -> { }
                is TransferProgress.Preparing -> {
                    _uiState.value = _uiState.value.copy(
                        transferProgress = progress.percent
                    )
                }
                is TransferProgress.Transferring -> {
                    val pct = progress.bytesTransferred.toFloat() / progress.totalBytes.toFloat()
                    _uiState.value = _uiState.value.copy(
                        transferProgress = pct,
                        sphereEnergy = 1f - pct,
                        sphereScale = 1f - pct * 0.5f,
                        sphereState = SphereState.Decaying(progress = pct)
                    )
                }
                is TransferProgress.Verifying -> { }
                is TransferProgress.Complete -> {
                    _uiState.value = _uiState.value.copy(
                        phase = TransferPhase.SPHERE_EXPLODING,
                        sphereState = SphereState.Exploding,
                        transferProgress = 1f,
                        sphereEnergy = 0f
                    )
                    delay(500)
                    _uiState.value = _uiState.value.copy(
                        phase = TransferPhase.TRANSFER_COMPLETE
                    )
                    emitSideEffect(SideEffect.ShowSuccess("transfer_${file.name}"))
                    Timber.d("Transferencia completada: ${file.name}")
                }
                is TransferProgress.Failed -> {
                    val error = progress.error
                    _uiState.value = _uiState.value.copy(
                        phase = TransferPhase.ERROR,
                        error = error,
                        sphereEnergy = 0f
                    )
                    emitSideEffect(SideEffect.ShowError(error.message))
                    Timber.e("Transferencia fallida: ${error.message}")
                }
            }
        }
    }

    private fun handleCancelTransfer() {
        transferJob?.cancel()
        discoveryJob?.cancel()

        viewModelScope.launch {
            cancelTransferUseCase("current_session")
        }

        _uiState.value = _uiState.value.copy(
            phase = TransferPhase.IDLE,
            sphereState = SphereState.Idle,
            transferProgress = 0f,
            sphereEnergy = 1f,
            sphereScale = 1f,
            spherePosition = Vector3.zero,
            error = null
        )
        Timber.d("Transferencia cancelada por el usuario")
    }

    private fun handleResetTransfer() {
        transferJob?.cancel()
        discoveryJob?.cancel()

        _uiState.value = TransferUiState()
        Timber.d("Estado de transferencia reiniciado")
    }

    private fun emitSideEffect(effect: SideEffect) {
        viewModelScope.launch {
            _sideEffects.emit(effect)
        }
    }
}
