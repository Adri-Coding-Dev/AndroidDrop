package com.androiddrop.security.keyexchange

import com.androiddrop.core.crypto.SessionKeys
import com.androiddrop.core.network.SocketManager

/**
 * Protocolo de intercambio de claves entre peers.
 *
 * POR QUÉ una interfaz: El intercambio de claves es una parte crítica de la
 * seguridad que debe ser testeable. La interfaz permite inyectar implementaciones
 * mock para pruebas unitarias del protocolo de transferencia, sin necesidad de
 * generar claves reales.
 *
 * El protocolo sigue 6 pasos:
 *   1. Generar par ECDH efímero (cada peer genera el suyo)
 *   2. Intercambiar claves públicas (PublicKeyExchange)
 *   3. Calcular secreto compartido con ECDH
 *   4. Derivar claves de sesión con HKDF-SHA256
 *   5. Autenticación mutua con nonce (AuthChallenge / AuthResponse)
 *   6. Verificar HMAC de handshake (ExchangeComplete)
 */
interface KeyExchangeProtocol {

    /**
     * Estado del intercambio de claves.
     */
    sealed class ExchangeState {
        /** No hay intercambio en curso. */
        data object IDLE : ExchangeState()

        /** Esperando recibir la clave pública del otro peer. */
        data object WAITING_FOR_PUBLIC_KEY : ExchangeState()

        /** Intercambiando mensajes de autenticación. */
        data object EXCHANGING : ExchangeState()

        /** Verificando la autenticación mutua. */
        data object VERIFYING : ExchangeState()

        /** Intercambio completado exitosamente, claves listas. */
        data object ESTABLISHED : ExchangeState()

        /** El intercambio falló. */
        data object FAILED : ExchangeState()
    }

    /**
     * Inicia el intercambio como peer activo (cliente).
     *
     * El peer activo envía primero su clave pública, luego responde al
     * desafío de autenticación del otro peer.
     *
     * @param connection Socket gestionado con el peer remoto.
     * @return SessionKeys si el intercambio fue exitoso, o un error.
     */
    suspend fun initiateExchange(connection: SocketManager.ManagedSocket): Result<SessionKeys>

    /**
     * Responde al intercambio como peer pasivo (servidor).
     *
     * El peer pasivo espera recibir la clave pública del otro, envía la suya,
     * y completa la autenticación mutua.
     *
     * @param connection Socket gestionado con el peer remoto.
     * @return SessionKeys si el intercambio fue exitoso, o un error.
     */
    suspend fun respondToExchange(connection: SocketManager.ManagedSocket): Result<SessionKeys>
}
