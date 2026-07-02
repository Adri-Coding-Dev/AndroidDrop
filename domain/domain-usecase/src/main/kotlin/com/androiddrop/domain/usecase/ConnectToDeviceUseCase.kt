package com.androiddrop.domain.usecase

import com.androiddrop.core.common.NearbyRepo
import com.androiddrop.domain.model.NearbyDevice
import com.androiddrop.domain.repository.DeviceRepository
import javax.inject.Inject

class ConnectToDeviceUseCase @Inject constructor(
    @NearbyRepo private val deviceRepository: DeviceRepository
) {
    suspend operator fun invoke(device: NearbyDevice): Result<Boolean> {
        val connected = deviceRepository.connectToDevice(device)
        if (!connected) return Result.failure(Exception("No se pudo conectar al dispositivo"))
        return deviceRepository.authenticateDevice(device)
    }
}
