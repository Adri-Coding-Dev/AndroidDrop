package com.androiddrop.security.crypto

import com.androiddrop.core.crypto.CryptoManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt que provee las dependencias del subsistema criptográfico.
 *
 * POR QUÉ Hilt para proveer SessionCryptoProvider: SessionCryptoProvider es
 * una interfaz con implementación concreta EcdhSessionCryptoProvider que
 * depende de CryptoManager. Usar Hilt permite que el resto del sistema
 * (KeyExchangeProtocol, servicio de transferencia) obtenga la implementación
 * sin conocer los detalles de construcción.
 */
@Module
@InstallIn(SingletonComponent::class)
object CryptoModule {

    /**
     * Provee la implementación por defecto de [SessionCryptoProvider].
     *
     * @param cryptoManager Gestor criptográfico de bajo nivel inyectado.
     * @return Implementación ECDH con Perfect Forward Secrecy.
     */
    @Provides
    @Singleton
    fun provideSessionCryptoProvider(
        cryptoManager: CryptoManager
    ): SessionCryptoProvider {
        return EcdhSessionCryptoProvider(cryptoManager)
    }
}
