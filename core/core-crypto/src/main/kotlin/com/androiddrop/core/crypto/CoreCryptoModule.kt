package com.androiddrop.core.crypto

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.security.SecureRandom
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreCryptoModule {

    @Binds
    @Singleton
    abstract fun bindCryptoManager(impl: EcdhCryptoManager): CryptoManager

    companion object {
        @Provides
        @Singleton
        fun provideSecureRandom(): SecureRandom = SecureRandom()
    }
}
