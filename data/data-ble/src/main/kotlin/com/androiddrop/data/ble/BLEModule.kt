package com.androiddrop.data.ble

import com.androiddrop.core.common.BleRepo
import com.androiddrop.domain.repository.DeviceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BLEModule {

    @Provides
    @Singleton
    @BleRepo
    fun provideDeviceRepository(repository: BLEDeviceRepository): DeviceRepository = repository
}
