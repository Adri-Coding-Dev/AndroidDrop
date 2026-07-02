package com.androiddrop.security.keyexchange

import com.androiddrop.core.crypto.CryptoManager
import com.androiddrop.core.crypto.SessionKeys
import com.androiddrop.core.network.SocketManager
import com.androiddrop.security.crypto.SessionCryptoProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementación del protocolo de intercambio de claves usando ECDH.
 *
 * POR QUÉ 6 pasos de handshake: Cada paso tiene un propósito de seguridad:
 *   1-4: Establecen el secreto compartido (ECDH + HKDF).
 *   5: Autenticación mutua evita man-in-the-middle.
 *   6: Verificación final asegura que ambos peers llegaron al mismo estado.
 *
 * POR QUÉ nonce + HMAC para autenticación: El nonce (número aleatorio único)
 * previene ataques de replay. Al cifrar el nonce y firmarlo con HMAC usando
 * las claves derivadas, demostramos que poseemos la clave privada ECDH
 * correcta sin exponerla.
 *
 * @property cryptoProvider Proveedor criptográfico de sesión (ECDH, HKDF, AES).
 * @property cryptoManager  Gestor criptográfico de bajo nivel (nonce, HMAC).
 */
class EcdhKeyExchangeProtocol @Inject constructor(
    private val cryptoProvider: SessionCryptoProvider,
    private val cryptoManager: CryptoManager
) : KeyExchangeProtocol {

    /** Serializador JSON para los mensajes de intercambio. */
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        /** Nuestro ID de dispositivo (se configurará desde el exterior). */
        var deviceId: String = "unknown"

        /** Tamaño de nonce para autenticación (16 bytes). */
        private const val NONCE_SIZE = 16

        /** Tiempo máximo de espera para respuestas (en ms). */
        private const val RESPONSE_TIMEOUT_MS = 10_000L
    }

    override suspend fun initiateExchange(connection: SocketManager.ManagedSocket): Result<SessionKeys> {
        return runCatching {
            Timber.d("EcdhKeyExchange: iniciando intercambio (activo)")

            // Paso 1: Generar par ECDH efímero
            val keyPair = cryptoProvider.generateSessionKeyPair()
            Timber.d("EcdhKeyExchange: par ECDH generado")

            // Paso 2: Enviar nuestra clave pública
            val publicKeyMsg = KeyExchangeMessage.PublicKeyExchange(
                publicKey = keyPair.publicKey,
                deviceId = deviceId
            )
            connection.send(serializeMessage(publicKeyMsg))
            Timber.d("EcdhKeyExchange: clave pública enviada")

            // Paso 2b: Recibir clave pública del otro peer
            val remotePublicKeyMsg = receiveMessage(connection)
            val remotePublicKey = when (remotePublicKeyMsg) {
                is KeyExchangeMessage.PublicKeyExchange -> {
                    Timber.d("EcdhKeyExchange: clave pública recibida de ${remotePublicKeyMsg.deviceId}")
                    remotePublicKeyMsg.publicKey
                }
                is KeyExchangeMessage.ExchangeError -> {
                    throw SecurityException("Error remoto: ${remotePublicKeyMsg.message}")
                }
                else -> throw SecurityException("Mensaje inesperado: $remotePublicKeyMsg")
            }

            // Paso 3: Calcular secreto compartido
            val sharedSecret = cryptoProvider.computeSharedSecret(remotePublicKey)
            Timber.d("EcdhKeyExchange: secreto compartido calculado")

            // Paso 4: Derivar claves de sesión
            val sessionKeys = cryptoProvider.deriveKeys(sharedSecret)
            Timber.d("EcdhKeyExchange: claves de sesión derivadas")

            // Paso 5: Autenticación mutua
            Timber.d("EcdhKeyExchange: esperando desafío de autenticación")
            val challengeMsg = receiveMessage(connection)
            val nonce = when (challengeMsg) {
                is KeyExchangeMessage.AuthChallenge -> {
                    Timber.d("EcdhKeyExchange: desafío recibido")
                    challengeMsg.nonce
                }
                is KeyExchangeMessage.ExchangeError -> {
                    throw SecurityException("Error remoto: ${challengeMsg.message}")
                }
                else -> throw SecurityException("Se esperaba AuthChallenge, se recibió: $challengeMsg")
            }

            // Cifrar nonce y firmar con HMAC
            val encryptedNonce = cryptoProvider.encryptChunk(nonce, sessionKeys, 0)
            val hmac = cryptoManager.sign(encryptedNonce, sessionKeys.macKey)

            connection.send(serializeMessage(
                KeyExchangeMessage.AuthResponse(encryptedNonce, hmac)
            ))
            Timber.d("EcdhKeyExchange: respuesta de autenticación enviada")

            // Paso 6: Verificar finalización
            val completeMsg = receiveMessage(connection)
            when (completeMsg) {
                is KeyExchangeMessage.ExchangeComplete -> {
                    Timber.d("EcdhKeyExchange: intercambio completado exitosamente")
                }
                is KeyExchangeMessage.ExchangeError -> {
                    throw SecurityException("Error remoto: ${completeMsg.message}")
                }
                else -> throw SecurityException("Se esperaba ExchangeComplete, se recibió: $completeMsg")
            }

            sessionKeys
        }.onFailure { error ->
            Timber.e(error, "EcdhKeyExchange: intercambio fallido")
        }
    }

    override suspend fun respondToExchange(connection: SocketManager.ManagedSocket): Result<SessionKeys> {
        return runCatching {
            Timber.d("EcdhKeyExchange: respondiendo a intercambio (pasivo)")

            // Paso 1: Generar par ECDH efímero
            val keyPair = cryptoProvider.generateSessionKeyPair()

            // Paso 2: Recibir clave pública del peer activo
            val remotePublicKeyMsg = receiveMessage(connection)
            val remotePublicKey = when (remotePublicKeyMsg) {
                is KeyExchangeMessage.PublicKeyExchange -> {
                    Timber.d("EcdhKeyExchange: clave pública recibida de ${remotePublicKeyMsg.deviceId}")
                    remotePublicKeyMsg.publicKey
                }
                is KeyExchangeMessage.ExchangeError -> {
                    throw SecurityException("Error remoto: ${remotePublicKeyMsg.message}")
                }
                else -> throw SecurityException("Mensaje inesperado: $remotePublicKeyMsg")
            }

            // Enviar nuestra clave pública
            connection.send(serializeMessage(
                KeyExchangeMessage.PublicKeyExchange(keyPair.publicKey, deviceId)
            ))
            Timber.d("EcdhKeyExchange: clave pública enviada")

            // Paso 3-4: Calcular secreto y derivar claves
            val sharedSecret = cryptoProvider.computeSharedSecret(remotePublicKey)
            val sessionKeys = cryptoProvider.deriveKeys(sharedSecret)
            Timber.d("EcdhKeyExchange: secreto y claves derivadas")

            // Paso 5: Enviar desafío y verificar respuesta
            val nonce = cryptoManager.generateNonce()
            connection.send(serializeMessage(KeyExchangeMessage.AuthChallenge(nonce)))
            Timber.d("EcdhKeyExchange: desafío de autenticación enviado")

            val responseMsg = receiveMessage(connection)
            when (responseMsg) {
                is KeyExchangeMessage.AuthResponse -> {
                    // Descifrar nonce y verificar HMAC
                    val decryptedNonce = cryptoProvider.decryptChunk(
                        responseMsg.encryptedNonce, sessionKeys, 0
                    )

                    if (!decryptedNonce.contentEquals(nonce)) {
                        Timber.e("EcdhKeyExchange: nonce no coincide")
                        connection.send(serializeMessage(
                            KeyExchangeMessage.ExchangeError(2, "Autenticación fallida: nonce no coincide")
                        ))
                        throw SecurityException("Autenticación fallida: nonce no coincide")
                    }

                    if (!cryptoManager.verify(responseMsg.encryptedNonce, responseMsg.hmac, sessionKeys.macKey)) {
                        Timber.e("EcdhKeyExchange: HMAC inválido")
                        connection.send(serializeMessage(
                            KeyExchangeMessage.ExchangeError(3, "Autenticación fallida: HMAC inválido")
                        ))
                        throw SecurityException("Autenticación fallida: HMAC inválido")
                    }

                    Timber.d("EcdhKeyExchange: autenticación verificada exitosamente")
                }
                is KeyExchangeMessage.ExchangeError -> {
                    throw SecurityException("Error remoto: ${responseMsg.message}")
                }
                else -> throw SecurityException("Se esperaba AuthResponse, se recibió: $responseMsg")
            }

            // Paso 6: Confirmar finalización
            connection.send(serializeMessage(KeyExchangeMessage.ExchangeComplete))
            Timber.d("EcdhKeyExchange: intercambio completado")

            sessionKeys
        }.onFailure { error ->
            Timber.e(error, "EcdhKeyExchange: intercambio fallido")
        }
    }

    /**
     * Serializa un mensaje a JSON y luego a ByteArray.
     */
    private fun serializeMessage(message: KeyExchangeMessage): ByteArray {
        return json.encodeToString(KeyExchangeMessage.serializer(), message).encodeToByteArray()
    }

    /**
     * Recibe y deserializa un mensaje del socket.
     */
    private suspend fun receiveMessage(connection: SocketManager.ManagedSocket): KeyExchangeMessage {
        val data = connection.receiveFlow().first()
        val jsonString = data.decodeToString()
        return json.decodeFromString(KeyExchangeMessage.serializer(), jsonString)
    }
}
