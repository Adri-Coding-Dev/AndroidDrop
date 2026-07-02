package com.androiddrop.domain.usecase

import com.androiddrop.core.common.NearbyRepo
import com.androiddrop.domain.model.NearbyDevice
import com.androiddrop.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class DiscoverDevicesUseCase @Inject constructor(
    @NearbyRepo private val deviceRepository: DeviceRepository
) {
    operator fun invoke(): Flow<NearbyDevice> {
        return deviceRepository.discoverDevices()
            .distinctUntilChanged { old, new -> old.deviceId == new.deviceId }
    }
}
