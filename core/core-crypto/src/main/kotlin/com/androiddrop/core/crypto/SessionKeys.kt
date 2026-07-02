package com.androiddrop.core.crypto

/**
 * Claves de sesión derivadas del secreto compartido ECDH.
 *
 * POR QUÉ tres claves separadas: El principio de separación de concerns
 * también aplica a criptografía. Cada clave tiene un propósito específico:
 *
 *   - [encKey]: Clave de cifrado AES-256-GCM.
 *     Si se compromete, el atacante puede descifrar datos pero no
 *     inyectar mensajes falsos (necesitaría macKey para eso).
 *
 *   - [macKey]: Clave de autenticación HMAC-SHA256.
 *     Si se compromete, el atacante puede falsificar mensajes pero
 *     no descifrar datos (necesitaría encKey para eso).
 *
 *   - [ivSeed]: Semilla para generar IVs deterministas por chunk.
 *     En lugar de transmitir el IV con cada chunk (lo que añadiría
 *     12 bytes por chunk), derivamos el IV del número de chunk usando
 *     esta semilla. Esto ahorra ancho de banda y es igual de seguro
 *     si la semilla permanece secreta.
 *
 * @property encKey Clave de cifrado (32 bytes para AES-256).
 * @property macKey Clave de autenticación HMAC (32 bytes).
 * @property ivSeed Semilla para derivación de IVs (32 bytes).
 */
data class SessionKeys(
    val encKey: ByteArray,
    val macKey: ByteArray,
    val ivSeed: ByteArray
)
