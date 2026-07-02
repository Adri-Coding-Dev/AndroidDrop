package com.androiddrop.security.keyexchange

import kotlinx.serialization.Serializable

/**
 * Mensajes del protocolo de intercambio de claves.
 *
 * POR QUÉ sealed class + @Serializable: El intercambio de claves es un
 * protocolo basado en mensajes donde cada paso intercambia un tipo de mensaje
 * diferente. Un sealed class asegura que el receptor maneje todos los casos
 * (compilador verifica exhaustividad con when()). La serialización con
 * kotlinx.serialization permite enviar estos mensajes como JSON por el socket.
 *
 * POR QUÉ ByteArray en mensajes: Las claves públicas, nonces y HMACs son datos
 * binarios. JSON los representa como base64, que es más compacto que hex y
 * estándar en la web.
 */
@Serializable
sealed class KeyExchangeMessage {

    /**
     * Mensaje inicial: intercambio de claves públicas.
     *
     * @property publicKey Clave pública ECDH del emisor (32 bytes, base64).
     * @property deviceId  ID único del dispositivo emisor.
     */
    @Serializable
    data class PublicKeyExchange(
        val publicKey: ByteArray,
        val deviceId: String
    ) : KeyExchangeMessage()

    /**
     * Desafío de autenticación: nonce aleatorio.
     *
     * El receptor debe cifrar este nonce y devolverlo firmado con HMAC.
     *
     * @property nonce 16 bytes aleatorios.
     */
    @Serializable
    data class AuthChallenge(
        val nonce: ByteArray
    ) : KeyExchangeMessage()

    /**
     * Respuesta al desafío de autenticación.
     *
     * @property encryptedNonce Nonce original cifrado con AES-256-GCM.
     * @property hmac           HMAC-SHA256 de todo el mensaje.
     */
    @Serializable
    data class AuthResponse(
        val encryptedNonce: ByteArray,
        val hmac: ByteArray
    ) : KeyExchangeMessage()

    /**
     * Mensaje de finalización: intercambio completado exitosamente.
     */
    @Serializable
    data object ExchangeComplete : KeyExchangeMessage()

    /**
     * Mensaje de error: algo salió mal durante el intercambio.
     *
     * @property code    Código de error (1 = clave inválida, 2 = autenticación fallida, etc.).
     * @property message Descripción legible del error.
     */
    @Serializable
    data class ExchangeError(
        val code: Int,
        val message: String
    ) : KeyExchangeMessage()
}
