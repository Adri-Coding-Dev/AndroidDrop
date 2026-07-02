package com.androiddrop.core.crypto

/**
 * Par de claves asimétricas (pública y privada).
 *
 * POR QUÉ data class propio vs java.security.KeyPair: La implementación
 * criptográfica de AndroidDrop usa la API lightweight de BouncyCastle en
 * lugar de JCA/JCE, por lo que java.security.KeyPair no es aplicable
 * (requiere objetos PublicKey/PrivateKey). Este data class encapsula las
 * claves como ByteArrays, que es el formato natural de X25519.
 *
 * @property publicKey Clave pública (32 bytes para X25519).
 * @property privateKey Clave privada (32 bytes para X25519).
 */
data class KeyPair(
    val publicKey: ByteArray,
    val privateKey: ByteArray
)
