package com.androiddrop.security.crypto

import com.androiddrop.core.crypto.KeyPair
import com.androiddrop.core.crypto.SessionKeys

/**
 * Proveedor de operaciones criptográficas para sesiones de transferencia.
 *
 * POR QUÉ una interfaz separada de [CryptoManager]: Mientras que CryptoManager
 * define operaciones criptográficas de bajo nivel (generación de pares, ECDH,
 * AES), SessionCryptoProvider encapsula el flujo completo de establecimiento
 * de sesión: generación de par efímero, cálculo de secreto compartido,
 * derivación de claves y cifrado/descifrado de chunks. Esto permite que el
 * protocolo de intercambio de claves use estas operaciones sin conocer los
 * detalles algorítmicos.
 *
 * POR QUÉ suspend functions: La generación de claves asimétricas y el cifrado
 * de chunks grandes son operaciones potencialmente lentas. Las suspend functions
 * permiten ejecutarlas en corrutinas sin bloquear el hilo principal.
 */
interface SessionCryptoProvider {

    /**
     * Genera un par de claves efímero para la sesión actual.
     *
     * Cada sesión genera un nuevo par ECDH para garantizar Perfect Forward
     * Secrecy: si una clave privada de sesión se ve comprometida, solo esa
     * sesión se ve afectada.
     *
     * @return Par (pública, privada) generado para esta sesión.
     */
    suspend fun generateSessionKeyPair(): KeyPair

    /**
     * Calcula el secreto compartido a partir de la clave pública del otro peer.
     *
     * Implementa ECDH (Elliptic Curve Diffie-Hellman) sobre Curve25519.
     * El secreto compartido es idéntico en ambos peers y nunca viaja por la red.
     *
     * @param theirPublic Clave pública del peer remoto (32 bytes).
     * @return Secreto compartido (32 bytes).
     */
    suspend fun computeSharedSecret(theirPublic: ByteArray): ByteArray

    /**
     * Deriva claves de sesión a partir del secreto compartido.
     *
     * Usa HKDF-SHA256 con salt específico de sesión para derivar:
     *   - encKey: Clave de cifrado AES-256-GCM (32 bytes)
     *   - macKey: Clave de autenticación HMAC (32 bytes)
     *   - ivSeed: Semilla para IVs deterministas (32 bytes)
     *
     * @param sharedSecret Secreto compartido de 32 bytes.
     * @return SessionKeys con encKey, macKey e ivSeed.
     */
    suspend fun deriveKeys(sharedSecret: ByteArray): SessionKeys

    /**
     * Cifra un chunk de datos.
     *
     * @param data       Datos a cifrar.
     * @param keys       Claves de sesión (usa encKey).
     * @param chunkIndex Índice del chunk para derivar IV único.
     * @return Datos cifrados + tag GCM.
     */
    suspend fun encryptChunk(data: ByteArray, keys: SessionKeys, chunkIndex: Long): ByteArray

    /**
     * Descifra un chunk de datos previamente cifrado.
     *
     * @param data       Datos cifrados + tag GCM.
     * @param keys       Claves de sesión (usa encKey).
     * @param chunkIndex Índice del chunk (debe coincidir con el usado en encrypt).
     * @return Datos en claro originales.
     */
    suspend fun decryptChunk(data: ByteArray, keys: SessionKeys, chunkIndex: Long): ByteArray

    /**
     * Genera un ID único para la sesión.
     *
     * @return UUID v4 como String.
     */
    fun generateSessionId(): String
}
