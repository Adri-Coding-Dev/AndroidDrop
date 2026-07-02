package com.androiddrop.core.network

import kotlinx.coroutines.flow.Flow

/**
 * Gestor de sockets para conexiones entre pares.
 *
 * POR QUÉ una interfaz separada: AndroidDrop necesita múltiples implementaciones
 * de socket (TCP directo, sockets seguros con TLS, WebSockets para Nearby).
 * Definir la interfaz SocketManager permite que el resto del sistema use
 * conexiones sin conocer la implementación concreta (principio de inversión
 * de dependencias).
 *
 * POR QUÉ suspend functions: Las operaciones de conexión de red son bloqueantes
 * por naturaleza. Las suspend functions de Kotlin permiten ejecutarlas en
 * corrutinas sin bloquear el hilo principal de UI.
 */
interface SocketManager {

    /**
     * Conecta a un peer remoto identificado por [connectionInfo].
     *
     * @param connectionInfo Información del peer destino (IP, puerto, etc.).
     * @return [ManagedSocket] listo para enviar/recibir datos.
     */
    suspend fun connectTo(connectionInfo: ConnectionInfo): ManagedSocket

    /**
     * Acepta una conexión entrante de un peer remoto.
     *
     * POR QUÉ acceptConnection vs connectTo: La asimetría es intencional.
     * connectTo inicia activamente la conexión (cliente), mientras que
     * acceptConnection espera pasivamente (servidor). Esto refleja la
     * naturaleza cliente-servidor del protocolo de descubrimiento.
     *
     * @return [ManagedSocket] listo para enviar/recibir datos.
     */
    suspend fun acceptConnection(): ManagedSocket

    /**
     * Socket gestionado con control de ciclo de vida completo.
     *
     * POR QUÉ ManagedSocket vs Socket crudo: Encapsula los detalles de
     * bajo nivel (InputStream/OutputStream, manejo de excepciones, cierre
     * seguro) y provee una API limpia basada en Flow para recepción.
     */
    interface ManagedSocket {
        /** Indica si el socket está conectado y funcional. */
        val isConnected: Boolean

        /**
         * Envía datos a través del socket.
         *
         * @param data Datos binarios a enviar.
         */
        suspend fun send(data: ByteArray)

        /**
         * Flow que emite los datos recibidos del peer.
         *
         * POR QUÉ Flow vs callback: Flow permite usar los operadores estándar
         * de Kotlin (map, filter, buffer) para procesar los datos entrantes.
         * Además, la cancelación de la corrutina que recolecta el flow cierra
         * automáticamente la recepción.
         *
         * @return Flow que emite ByteArray por cada mensaje recibido.
         */
        fun receiveFlow(): Flow<ByteArray>

        /**
         * Cierra la conexión de forma segura.
         *
         * Libera todos los recursos asociados (sockets, streams, buffers).
         * Es seguro llamarlo múltiples veces.
         */
        suspend fun close()
    }
}
