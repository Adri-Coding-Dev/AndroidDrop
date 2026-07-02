package com.androiddrop.core.crypto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator
import org.bouncycastle.crypto.modes.GCMBlockCipher
import org.bouncycastle.crypto.params.AEADParameters
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import timber.log.Timber

/**
 * Implementación de [CryptoManager] usando Curva25519/X25519 (BouncyCastle lightweight API).
 *
 * POR QUÉ API lightweight de BouncyCastle vs JCE: La API lightweight de
 * BouncyCastle (org.bouncycastle.crypto.*) NO requiere registrar el provider
 * de seguridad de Java, lo que evita conflictos con Android Keystore y
 * simplifica el classpath. Además, X25519 no está disponible en JCE estándar
 * de Android, por lo que la API lightweight es la única opción portable.
 *
 * POR QUÉ X25519 vs P-256: X25519 (Curve25519) es más rápida, más segura
 * (menos probabilidad de implementación vulnerable), y produce claves más
 * pequeñas (32 bytes vs 65 bytes para P-256 comprimida). Es el estándar
 * moderno para intercambio de claves (RFC 7748).
 *
 * @param secureRandom Fuente de aleatoriedad criptográficamente segura.
 */
class EcdhCryptoManager @Inject constructor(
    private val secureRandom: SecureRandom
) : CryptoManager {

    companion object {
        /** Tamaño del tag de autenticación GCM en bits (128 bits = 16 bytes). */
        private const val GCM_TAG_LENGTH_BITS = 128

        /** Tamaño del IV GCM: 12 bytes (recomendado por NIST para GCM). */
        private const val GCM_IV_LENGTH = 12

        /** Algoritmo HMAC para derivación y autenticación. */
        private const val HMAC_ALGORITHM = "HmacSHA256"

        /** Tamaño de claves derivadas (32 bytes = 256 bits). */
        private const val KEY_SIZE_BYTES = 32

        /** Contexto de derivación HKDF (domain separation). */
        private const val HKDF_CONTEXT = "AndroidDrop-HKDF-v1"
    }

    // --- Pares de claves asimétricas ---
    private var keyPair: Pair<X25519PrivateKeyParameters, X25519PublicKeyParameters>? = null

    /**
     * Genera un par de claves X25519 usando la API lightweight de BouncyCastle.
     *
     * @return KeyPair con clave pública y privada.
     */
    override suspend fun generateKeyPair(): KeyPair = withContext(Dispatchers.Default) {
        try {
            val generator = X25519KeyPairGenerator()
            generator.init(X25519KeyGenerationParameters(secureRandom))
            val bcKeyPair = generator.generateKeyPair()

            val privateKey = bcKeyPair.private as X25519PrivateKeyParameters
            val publicKey = bcKeyPair.public as X25519PublicKeyParameters

            keyPair = Pair(privateKey, publicKey)

            KeyPair(
                publicKey.encoded,
                privateKey.encoded
            )
        } catch (e: Exception) {
            Timber.e(e, "Error al generar par de claves X25519")
            throw e
        }
    }

    /**
     * Deriva un secreto compartido de 32 bytes usando ECDH con X25519.
     *
     * @param theirPublicKey Clave pública del peer (32 bytes raw X25519).
     * @return Secreto compartido (32 bytes).
     */
    override suspend fun deriveSharedSecret(theirPublicKey: ByteArray): ByteArray =
        withContext(Dispatchers.Default) {
            try {
                val (privateKey, _) = keyPair
                    ?: throw IllegalStateException("generateKeyPair() debe llamarse antes")

                val peerPublicKey = X25519PublicKeyParameters(theirPublicKey, 0)
                val sharedSecret = ByteArray(KEY_SIZE_BYTES)
                privateKey.generateSecret(peerPublicKey, sharedSecret, 0)

                sharedSecret
            } catch (e: Exception) {
                Timber.e(e, "Error al derivar secreto compartido")
                throw e
            }
        }

    /**
     * Deriva claves de sesión usando HKDF-SHA256 (RFC 5869).
     *
     * HKDF expande el secreto compartido de 32 bytes en 96 bytes:
     *   - [SessionKeys.encKey]: cifrado AES-256-GCM (32 bytes)
     *   - [SessionKeys.macKey]: autenticación HMAC-SHA256 (32 bytes)
     *   - [SessionKeys.ivSeed]: semilla para IVs deterministas (32 bytes)
     */
    override fun deriveSessionKeys(sharedSecret: ByteArray): SessionKeys {
        val salt = "AndroidDrop-Salt-v1".toByteArray()
        val info = HKDF_CONTEXT.toByteArray()

        // HKDF-Extract: PRK = HMAC-SHA256(salt, sharedSecret)
        val prk = hmacSha256(sharedSecret, salt)

        // HKDF-Expand: OKM = HKDF-Expand(PRK, info, 96)
        val okm = hkdfExpand(prk, info, KEY_SIZE_BYTES * 3)

        return SessionKeys(
            encKey = okm.copyOfRange(0, KEY_SIZE_BYTES),
            macKey = okm.copyOfRange(KEY_SIZE_BYTES, KEY_SIZE_BYTES * 2),
            ivSeed = okm.copyOfRange(KEY_SIZE_BYTES * 2, KEY_SIZE_BYTES * 3)
        )
    }

    /**
     * Cifra datos con AES-256-GCM.
     *
     * El IV se deriva determinísticamente del número de chunk:
     *   IV = HMAC-SHA256(ivSeed, chunkNumber)[0..11]
     *
     * Esto elimina la necesidad de transmitir el IV con cada chunk,
     * ahorrando 12 bytes por chunk (~11MB en una transferencia de 1GB).
     *
     * @return ciphertext + GCM authentication tag (16 bytes al final).
     */
    override suspend fun encrypt(data: ByteArray, key: SessionKeys, chunkNumber: Long): ByteArray =
        withContext(Dispatchers.Default) {
            try {
                val iv = deriveIv(key.ivSeed, chunkNumber)
                val cipher = GCMBlockCipher(AESEngine())
                val params = AEADParameters(
                    KeyParameter(key.encKey),
                    GCM_TAG_LENGTH_BITS,
                    iv,
                    ByteArray(0) // associated data (vacío para chunks)
                )
                cipher.init(true, params)

                val outputSize = cipher.getOutputSize(data.size)
                val output = ByteArray(outputSize)
                val len = cipher.processBytes(data, 0, data.size, output, 0)
                cipher.doFinal(output, len)

                output
            } catch (e: Exception) {
                Timber.e(e, "Error al cifrar datos")
                throw e
            }
        }

    /**
     * Descifra datos cifrados con [encrypt].
     *
     * @return plaintext original.
     */
    override suspend fun decrypt(encryptedData: ByteArray, key: SessionKeys, chunkNumber: Long): ByteArray =
        withContext(Dispatchers.Default) {
            try {
                val iv = deriveIv(key.ivSeed, chunkNumber)
                val cipher = GCMBlockCipher(AESEngine())
                val params = AEADParameters(
                    KeyParameter(key.encKey),
                    GCM_TAG_LENGTH_BITS,
                    iv,
                    ByteArray(0)
                )
                cipher.init(false, params)

                val outputSize = cipher.getOutputSize(encryptedData.size)
                val output = ByteArray(outputSize)
                val len = cipher.processBytes(encryptedData, 0, encryptedData.size, output, 0)
                cipher.doFinal(output, len)

                output
            } catch (e: Exception) {
                Timber.e(e, "Error al descifrar datos")
                throw e
            }
        }

    /**
     * Genera un nonce de 16 bytes para desafíos de autenticación.
     */
    override fun generateNonce(): ByteArray {
        val nonce = ByteArray(16)
        secureRandom.nextBytes(nonce)
        return nonce
    }

    /**
     * Firma datos con HMAC-SHA256.
     */
    override fun sign(data: ByteArray, macKey: ByteArray): ByteArray {
        return hmacSha256(data, macKey)
    }

    /**
     * Verifica firma HMAC-SHA256 en tiempo constante.
     */
    override fun verify(data: ByteArray, signature: ByteArray, macKey: ByteArray): Boolean {
        val computed = hmacSha256(data, macKey)
        return constantTimeEquals(computed, signature)
    }

    // --- Funciones auxiliares ---

    /**
     * Deriva un IV único de 12 bytes para cada chunk.
     *
     * IV = HMAC-SHA256(ivSeed, chunkNumber)[0..11]
     *
     * POR QUÉ truncar a 12 bytes: GCM requiere un nonce de exactamente 12 bytes
     * para rendimiento óptimo (si es más largo, aplica GHASH adicional).
     */
    private fun deriveIv(ivSeed: ByteArray, chunkNumber: Long): ByteArray {
        val chunkBytes = byteArrayOf(
            (chunkNumber shr 56).toByte(),
            (chunkNumber shr 48).toByte(),
            (chunkNumber shr 40).toByte(),
            (chunkNumber shr 32).toByte(),
            (chunkNumber shr 24).toByte(),
            (chunkNumber shr 16).toByte(),
            (chunkNumber shr 8).toByte(),
            chunkNumber.toByte()
        )
        val hash = hmacSha256(chunkBytes, ivSeed)
        return hash.copyOf(GCM_IV_LENGTH)
    }

    /**
     * HMAC-SHA256 estándar.
     */
    private fun hmacSha256(data: ByteArray, key: ByteArray): ByteArray {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(SecretKeySpec(key, HMAC_ALGORITHM))
        return mac.doFinal(data)
    }

    /**
     * HKDF-Expand (RFC 5869 sección 2.3).
     *
     * Genera [length] bytes a partir de la clave pseudoaleatoria [prk].
     */
    private fun hkdfExpand(prk: ByteArray, info: ByteArray, length: Int): ByteArray {
        val hashLen = 32
        val blocks = (length + hashLen - 1) / hashLen
        val result = mutableListOf<ByteArray>()
        var previous = ByteArray(0)

        for (i in 1..blocks) {
            val input = previous + info + byteArrayOf(i.toByte())
            val block = hmacSha256(input, prk)
            result.add(block)
            previous = block
        }

        return result
            .flatMap { it.toList() }
            .take(length)
            .toByteArray()
    }

    /**
     * Comparación en tiempo constante para prevenir ataques de timing.
     *
     * POR QUÉ constante vs Arrays.equals(): Ver [CryptoUtils.constantTimeEquals].
     */
    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }
}
