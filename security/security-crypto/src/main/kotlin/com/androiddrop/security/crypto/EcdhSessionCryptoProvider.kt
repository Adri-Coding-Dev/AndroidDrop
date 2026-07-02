package com.androiddrop.security.crypto

import com.androiddrop.core.crypto.CryptoManager
import com.androiddrop.core.crypto.KeyPair
import com.androiddrop.core.crypto.SessionKeys
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/**
 * Implementación de [SessionCryptoProvider] usando ECDH con Curve25519.
 *
 * POR QUÉ Curve25519 vs P-256/P-384: Curve25519 es más rápida que P-256
 * (~3x en generación de claves) y más resistente a side-channel attacks.
 * Además, el estándar X25519 está diseñado específicamente para evitar
 * vulnerabilidades de implementación comunes en ECDH.
 *
 * POR QUÉ claves efímeras por sesión (Perfect Forward Secrecy): Cada sesión
 * de transferencia genera un nuevo par de claves efímeras. Si la clave
 * privada de una sesión se ve comprometida, el atacante solo puede descifrar
 * esa sesión, no las anteriores ni las futuras. Esto se logra generando un
 * nuevo par ECDH al inicio de cada sesión y descartándolo al finalizar.
 *
 * El flujo completo de establecimiento de sesión es:
 *   1. Generar par ECDH efímero (generateSessionKeyPair)
 *   2. Intercambiar claves públicas con el otro peer (vía KeyExchangeProtocol)
 *   3. Calcular shared secret: ECDH(privada_local, pública_remota)
 *   4. Derivar claves de sesión: HKDF-SHA256(shared_secret)
 *   5. Cifrar chunks con AES-256-GCM usando encKey e IV derivado por chunk
 *
 * @property cryptoManager Gestor criptográfico de bajo nivel que implementa
 *                         las primitivas (ECDH, AES-256-GCM, HKDF).
 */
class EcdhSessionCryptoProvider @Inject constructor(
    private val cryptoManager: CryptoManager
) : SessionCryptoProvider {

    /** Par de claves efímero de la sesión actual. */
    private var sessionKeyPair: KeyPair? = null

    override suspend fun generateSessionKeyPair(): KeyPair {
        Timber.d("EcdhSessionCryptoProvider: generando par ECDH efímero")
        val keyPair = cryptoManager.generateKeyPair()
        sessionKeyPair = keyPair
        Timber.d("EcdhSessionCryptoProvider: par generado exitosamente")
        return keyPair
    }

    override suspend fun computeSharedSecret(theirPublic: ByteArray): ByteArray {
        Timber.d("EcdhSessionCryptoProvider: calculando secreto compartido ECDH")
        val sharedSecret = cryptoManager.deriveSharedSecret(theirPublic)
        Timber.d("EcdhSessionCryptoProvider: secreto compartido calculado (${sharedSecret.size} bytes)")
        return sharedSecret
    }

    override suspend fun deriveKeys(sharedSecret: ByteArray): SessionKeys {
        Timber.d("EcdhSessionCryptoProvider: derivando claves de sesión con HKDF-SHA256")
        val keys = cryptoManager.deriveSessionKeys(sharedSecret)
        Timber.d("EcdhSessionCryptoProvider: claves derivadas (encKey=${keys.encKey.size}, macKey=${keys.macKey.size}, ivSeed=${keys.ivSeed.size})")
        return keys
    }

    override suspend fun encryptChunk(data: ByteArray, keys: SessionKeys, chunkIndex: Long): ByteArray {
        Timber.v("EcdhSessionCryptoProvider: cifrando chunk $chunkIndex (${data.size} bytes)")
        return cryptoManager.encrypt(data, keys, chunkIndex)
    }

    override suspend fun decryptChunk(data: ByteArray, keys: SessionKeys, chunkIndex: Long): ByteArray {
        Timber.v("EcdhSessionCryptoProvider: descifrando chunk $chunkIndex (${data.size} bytes)")
        return cryptoManager.decrypt(data, keys, chunkIndex)
    }

    override fun generateSessionId(): String {
        val sessionId = UUID.randomUUID().toString()
        Timber.d("EcdhSessionCryptoProvider: ID de sesión generado: $sessionId")
        return sessionId
    }
}
