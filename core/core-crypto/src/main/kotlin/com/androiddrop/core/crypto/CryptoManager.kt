package com.androiddrop.core.crypto

/**
 * Gestor de operaciones criptográficas para AndroidDrop.
 *
 * POR QUÉ esta interfaz: AndroidDrop necesita múltiples operaciones criptográficas
 * (intercambio de claves, cifrado simétrico, autenticación de mensajes) que
 * forman la base de la seguridad del protocolo (SDD-09-Seguridad.md). Definir
 * una interfaz permite probar el sistema con implementaciones mock y swap de
 * algoritmos sin cambiar el resto del código.
 *
 * El flujo completo es:
 *   1. Cada peer genera su par de claves (generateKeyPair)
 *   2. Intercambian claves públicas y derivan un secreto compartido (deriveSharedSecret)
 *   3. Del secreto compartido derivan claves de sesión (deriveSessionKeys)
 *   4. Cifran/des cifran chunks con AES-256-GCM (encrypt/decrypt)
 *   5. Autentican mensajes con HMAC-SHA256 (sign/verify)
 */
interface CryptoManager {

    /**
     * Genera un par de llaves (pública/privada) para el peer local.
     *
     * POR QUÉ KeyPair vs claves separadas: Encapsular ambas claves en un
     * solo data class asegura que la clave privada nunca sea accesible sin
     * su par público correspondiente, reduciendo riesgo de exposición.
     *
     * @return KeyPair con la clave pública y privada generadas.
     */
    suspend fun generateKeyPair(): KeyPair

    /**
     * Deriva un secreto compartido a partir de la clave pública del otro peer.
     *
     * Implementa ECDH (Elliptic Curve Diffie-Hellman) sobre Curve25519.
     * Ambos peers generan el mismo secreto compartido sin que este viaje
     * nunca por la red.
     *
     * @param theirPublicKey Clave pública del peer remoto (bytes).
     * @return Secreto compartido de 32 bytes.
     */
    suspend fun deriveSharedSecret(theirPublicKey: ByteArray): ByteArray

    /**
     * Deriva claves de sesión a partir del secreto compartido.
     *
     * Usa HKDF-SHA256 para derivar tres claves de 32 bytes cada una:
     *   - encKey: Para cifrado AES-256-GCM
     *   - macKey: Para HMAC-SHA256 (autenticación de mensajes)
     *   - ivSeed: Semilla para generar IVs únicos por chunk
     *
     * @param sharedSecret Secreto compartido de 32 bytes.
     * @return SessionKeys con encKey, macKey e ivSeed.
     */
    fun deriveSessionKeys(sharedSecret: ByteArray): SessionKeys

    /**
     * Cifra datos con AES-256-GCM.
     *
     * @param data Datos en claro a cifrar.
     * @param key Claves de sesión (encKey para cifrado).
     * @param chunkNumber Número de chunk (para IV único por chunk).
     * @return Datos cifrados + tag GCM (12 bytes adicionales al final).
     */
    suspend fun encrypt(data: ByteArray, key: SessionKeys, chunkNumber: Long): ByteArray

    /**
     * Descifra datos previamente cifrados con [encrypt].
     *
     * @param encryptedData Datos cifrados + tag GCM.
     * @param key Claves de sesión (encKey para descifrado).
     * @param chunkNumber Número de chunk (debe coincidir con el usado en encrypt).
     * @return Datos en claro originales.
     */
    suspend fun decrypt(encryptedData: ByteArray, key: SessionKeys, chunkNumber: Long): ByteArray

    /**
     * Genera un nonce aleatorio para desafíos de autenticación.
     *
     * POR QUÉ nonces: Los nonces (number-used-once) previenen ataques de
     * replay. Cada desafío de autenticación usa un nonce diferente, por lo
     * que una respuesta capturada no puede ser reutilizada.
     *
     * @return 16 bytes aleatorios criptográficamente seguros.
     */
    fun generateNonce(): ByteArray

    /**
     * Firma datos con HMAC-SHA256 para autenticación.
     *
     * @param data Datos a firmar.
     * @param macKey Clave HMAC (de SessionKeys.macKey).
     * @return Firma HMAC-SHA256 (32 bytes).
     */
    fun sign(data: ByteArray, macKey: ByteArray): ByteArray

    /**
     * Verifica una firma HMAC-SHA256.
     *
     * @param data Datos originales.
     * @param signature Firma a verificar.
     * @param macKey Clave HMAC (de SessionKeys.macKey).
     * @return true si la firma es válida, false en caso contrario.
     */
    fun verify(data: ByteArray, signature: ByteArray, macKey: ByteArray): Boolean
}
