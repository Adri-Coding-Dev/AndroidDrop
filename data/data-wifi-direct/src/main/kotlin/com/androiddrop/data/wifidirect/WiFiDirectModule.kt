package com.androiddrop.data.wifidirect

import com.androiddrop.core.common.WifiDirectRepo
import com.androiddrop.domain.repository.TransferRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WiFiDirectModule {

    @Provides
    @Singleton
    @WifiDirectRepo
    fun provideTransferRepository(repository: WiFiDirectTransferRepository): TransferRepository =
        repository
}
