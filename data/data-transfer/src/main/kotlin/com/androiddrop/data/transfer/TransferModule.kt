package com.androiddrop.data.transfer

import com.androiddrop.core.common.DefaultRepo
import com.androiddrop.domain.repository.TransferRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TransferModule {

    @Provides
    @Singleton
    @DefaultRepo
    fun provideTransferRepository(repository: TransferRepositoryImpl): TransferRepository =
        repository
}
