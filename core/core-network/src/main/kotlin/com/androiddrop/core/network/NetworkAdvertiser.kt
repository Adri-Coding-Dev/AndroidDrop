package com.androiddrop.core.network

/**
 * Anunciante de servicios en la red local.
 *
 * POR QUÉ un anunciante: Para que dos dispositivos AndroidDrop se encuentren,
 * uno debe anunciar su presencia (servidor) y el otro debe escanear (cliente).
 * NetworkAdvertiser es el rol de "servidor": emite señales de presencia para
 * que los escáneres cercanos lo detecten.
 *
 * Las responsabilidades del anunciante son:
 *   1. Emitir broadcast de presencia en la red local
 *   2. Escuchar conexiones entrantes de peers que responden al anuncio
 *   3. Notificar cuando un peer es encontrado o cuando falla el anuncio
 */
interface NetworkAdvertiser {

    /**
     * Comienza a anunciar este dispositivo en la red.
     *
     * @param serviceName Nombre del servicio visible para otros peers (ej: "AndroidDrop").
     * @param listener Callback para eventos de descubrimiento y errores.
     */
    fun startAdvertising(serviceName: String, listener: AdvertisingListener)

    /**
     * Detiene el anuncio de presencia en la red.
     *
     * POR QUÉ método explícito: El anunciante debe liberar recursos de red
     * cuando ya no necesita ser visible (ej: al salir de la pantalla de
     * transferencia). Llamar a stopAdvertising() detiene los broadcasts
     * y cierra los sockets de escucha.
     */
    fun stopAdvertising()

    /**
     * Listener para eventos del anunciante.
     */
    interface AdvertisingListener {

        /**
         * Un peer ha sido encontrado y está listo para conectar.
         *
         * @param connectionInfo Información del peer descubierto.
         */
        fun onPeerFound(connectionInfo: ConnectionInfo)

        /**
         * El anuncio ha fallado por un error técnico.
         *
         * @param error Causa del fallo (red no disponible, permiso denegado, etc.).
         */
        fun onAdvertisingFailed(error: Throwable)
    }
}
