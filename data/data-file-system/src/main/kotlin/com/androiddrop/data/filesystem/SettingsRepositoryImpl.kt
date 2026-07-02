package com.androiddrop.data.filesystem

import android.content.Context
import android.content.SharedPreferences
import com.androiddrop.domain.repository.SettingsRepository
import com.androiddrop.domain.repository.ThemeMode
import com.androiddrop.domain.repository.TransferQuality
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("androiddrop_settings", Context.MODE_PRIVATE)

    private val themeMode = MutableStateFlow(loadThemeMode())
    private val reducedMotion = MutableStateFlow(loadReducedMotion())
    private val transferQuality = MutableStateFlow(loadTransferQuality())
    private val storageLocation = MutableStateFlow(loadStorageLocation())

    override fun getThemeMode(): Flow<ThemeMode> = themeMode
    override suspend fun setThemeMode(mode: ThemeMode) {
        themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    override fun isReducedMotionEnabled(): Flow<Boolean> = reducedMotion
    override suspend fun setReducedMotionEnabled(enabled: Boolean) {
        reducedMotion.value = enabled
        prefs.edit().putBoolean("reduced_motion", enabled).apply()
    }

    override fun getTransferQuality(): Flow<TransferQuality> = transferQuality
    override suspend fun setTransferQuality(quality: TransferQuality) {
        transferQuality.value = quality
        prefs.edit().putString("transfer_quality", quality.name).apply()
    }

    override fun getStorageLocation(): Flow<String> = storageLocation
    override suspend fun setStorageLocation(uri: String) {
        storageLocation.value = uri
        prefs.edit().putString("storage_location", uri).apply()
    }

    private fun loadThemeMode(): ThemeMode {
        return try {
            ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            Timber.e(e, "Error cargando theme_mode")
            ThemeMode.SYSTEM
        }
    }

    private fun loadReducedMotion(): Boolean {
        return prefs.getBoolean("reduced_motion", false)
    }

    private fun loadTransferQuality(): TransferQuality {
        return try {
            TransferQuality.valueOf(
                prefs.getString("transfer_quality", TransferQuality.STANDARD.name) ?: TransferQuality.STANDARD.name
            )
        } catch (e: Exception) {
            Timber.e(e, "Error cargando transfer_quality")
            TransferQuality.STANDARD
        }
    }

    private fun loadStorageLocation(): String {
        return prefs.getString("storage_location", "") ?: ""
    }
}
