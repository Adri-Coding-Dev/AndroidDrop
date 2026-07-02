package com.androiddrop.domain.repository

import com.androiddrop.domain.model.NearbyDevice
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun discoverDevices(): Flow<NearbyDevice>
    suspend fun connectToDevice(device: NearbyDevice): Boolean
    suspend fun disconnectDevice(deviceId: String)
    fun getConnectedDevice(): Flow<NearbyDevice?>
    suspend fun authenticateDevice(device: NearbyDevice): Result<Boolean>
}
