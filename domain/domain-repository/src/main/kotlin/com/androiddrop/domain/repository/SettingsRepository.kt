package com.androiddrop.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
    fun isReducedMotionEnabled(): Flow<Boolean>
    suspend fun setReducedMotionEnabled(enabled: Boolean)
    fun getTransferQuality(): Flow<TransferQuality>
    suspend fun setTransferQuality(quality: TransferQuality)
    fun getStorageLocation(): Flow<String>
    suspend fun setStorageLocation(uri: String)
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class TransferQuality {
    STANDARD,
    HIGH,
    MAX
}
