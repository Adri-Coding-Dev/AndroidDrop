package com.androiddrop.core.crypto

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Utilidades criptográficas generales de AndroidDrop.
 *
 * POR QUÉ un objeto separado vs métodos en CryptoManager: Estas funciones
 * son operaciones criptográficas puras que no requieren estado ni contexto
 * de sesión. Separarlas en un objeto permite usarlas en cualquier lugar
 * sin tener que inyectar CryptoManager.
 */
object CryptoUtils {

    /** Algoritmo hash SHA-256. */
    private const val SHA256 = "SHA-256"

    /** Algoritmo HMAC-SHA256. */
    private const val HMAC_SHA256 = "HmacSHA256"

    /** Fuente de aleatoriedad criptográficamente segura. */
    private val secureRandom = SecureRandom()

    /**
     * Genera [size] bytes aleatorios criptográficamente seguros.
     *
     * POR QUÉ SecureRandom vs Random: Random.java usa una semilla predecible
     * (LGC - Linear Congruential Generator). SecureRandom usa entropía del
     * sistema (/dev/urandom en Linux), produciendo bytes impredecibles
     * adecuados para claves y nonces criptográficos.
     *
     * @param size Número de bytes a generar.
     * @return ByteArray con bytes aleatorios seguros.
     */
    fun generateRandomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        secureRandom.nextBytes(bytes)
        return bytes
    }

    /**
     * Calcula el hash SHA-256 de [data].
     *
     * @param data Datos a hashear.
     * @return Hash SHA-256 (32 bytes).
     */
    fun sha256(data: ByteArray): ByteArray {
        return MessageDigest.getInstance(SHA256).digest(data)
    }

    /**
     * Calcula HMAC-SHA256 de [data] usando [key].
     *
     * @param data Datos a autenticar.
     * @param key Clave HMAC.
     * @return HMAC-SHA256 (32 bytes).
     */
    fun hmacSha256(data: ByteArray, key: ByteArray): ByteArray {
        val mac = Mac.getInstance(HMAC_SHA256)
        mac.init(SecretKeySpec(key, HMAC_SHA256))
        return mac.doFinal(data)
    }

    /**
     * Compara dos ByteArrays en tiempo constante.
     *
     * POR QUÉ tiempo constante: Ver [EcdhCryptoManager.constantTimeEquals].
     * La comparación en tiempo constante previene ataques de timing side-channel
     * donde un atacante mide el tiempo de respuesta para adivinar la firma.
     *
     * @param a Primer array.
     * @param b Segundo array.
     * @return true si son idénticos byte a byte, false en caso contrario.
     */
    fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }
}
