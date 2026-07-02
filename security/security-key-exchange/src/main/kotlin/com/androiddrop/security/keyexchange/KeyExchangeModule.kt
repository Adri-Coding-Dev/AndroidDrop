package com.androiddrop.security.keyexchange

import com.androiddrop.core.crypto.CryptoManager
import com.androiddrop.security.crypto.SessionCryptoProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt que provee las dependencias del protocolo de intercambio de claves.
 *
 * POR QUÉ un módulo separado de CryptoModule: KeyExchangeProtocol depende de
 * SessionCryptoProvider y del protocolo de red (SocketManager). Separar los
 * módulos permite que el intercambio de claves pueda ser mockeado independientemente
 * del subsistema criptográfico en las pruebas.
 */
@Module
@InstallIn(SingletonComponent::class)
object KeyExchangeModule {

    /**
     * Provee la implementación por defecto de [KeyExchangeProtocol].
     *
     * @param cryptoProvider Proveedor criptográfico de sesión (ECDH, AES).
     * @param cryptoManager  Gestor criptográfico de bajo nivel (HMAC, nonce).
     * @return Implementación ECDH del protocolo de intercambio de claves.
     */
    @Provides
    @Singleton
    fun provideKeyExchangeProtocol(
        cryptoProvider: SessionCryptoProvider,
        cryptoManager: CryptoManager
    ): KeyExchangeProtocol {
        return EcdhKeyExchangeProtocol(cryptoProvider, cryptoManager)
    }
}
