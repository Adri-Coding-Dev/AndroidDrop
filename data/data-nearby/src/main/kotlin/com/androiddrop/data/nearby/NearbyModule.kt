package com.androiddrop.data.nearby

import com.androiddrop.core.common.NearbyRepo
import com.androiddrop.domain.repository.DeviceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NearbyModule {

    @Provides
    @Singleton
    @NearbyRepo
    fun provideDeviceRepository(repository: NearbyDeviceRepository): DeviceRepository = repository
}
