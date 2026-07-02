package com.androiddrop.feature.settings

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddrop.domain.repository.SettingsRepository
import com.androiddrop.domain.repository.ThemeMode
import com.androiddrop.domain.repository.TransferQuality
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val reducedMotion: Boolean = false,
    val transferQuality: TransferQuality = TransferQuality.STANDARD,
    val storageLocation: String = "",
    val appVersion: String = ""
)

sealed interface SettingsIntent {
    data class SetTheme(val mode: ThemeMode) : SettingsIntent
    data object ToggleReducedMotion : SettingsIntent
    data class SetQuality(val quality: TransferQuality) : SettingsIntent
    data class SetStorageLocation(val uri: String) : SettingsIntent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.getThemeMode(),
        settingsRepository.isReducedMotionEnabled(),
        settingsRepository.getTransferQuality(),
        settingsRepository.getStorageLocation()
    ) { themeMode, reducedMotion, transferQuality, storageLocation ->
        SettingsUiState(
            themeMode = themeMode,
            reducedMotion = reducedMotion,
            transferQuality = transferQuality,
            storageLocation = storageLocation,
            appVersion = getAppVersion()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SetTheme -> {
                viewModelScope.launch {
                    settingsRepository.setThemeMode(intent.mode)
                }
            }
            is SettingsIntent.ToggleReducedMotion -> {
                viewModelScope.launch {
                    val current = uiState.value.reducedMotion
                    settingsRepository.setReducedMotionEnabled(!current)
                }
            }
            is SettingsIntent.SetQuality -> {
                viewModelScope.launch {
                    settingsRepository.setTransferQuality(intent.quality)
                }
            }
            is SettingsIntent.SetStorageLocation -> {
                viewModelScope.launch {
                    settingsRepository.setStorageLocation(intent.uri)
                }
            }
        }
    }

    private fun getAppVersion(): String {
        return try {
            val pkgInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pkgInfo.versionName ?: "Desconocido"
        } catch (e: PackageManager.NameNotFoundException) {
            "Desconocido"
        }
    }
}
