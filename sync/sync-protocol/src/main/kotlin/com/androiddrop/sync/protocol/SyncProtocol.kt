package com.androiddrop.sync.protocol

import kotlinx.coroutines.flow.Flow

/**
 * Protocolo de sincronización bidireccional entre peers.
 *
 * POR QUÉ interfaz vs implementación concreta: La sincronización es un
 * componente crítico que debe ser testeable con mocks. La interfaz permite
 * probar el sistema de interpolación y compensación de latencia sin una
 * conexión de red real.
 *
 * El protocolo provee:
 *   - Envío de frames de estado a 20-30 Hz
 *   - Recepción asíncrona mediante Flow
 *   - Negociación de clock (RTT calculation)
 *   - Sincronización maestro-esclavo
 */
interface SyncProtocol {

    /**
     * Envía un frame de sincronización al peer remoto.
     *
     * El emisor es el "clock maestro": define la línea de tiempo que el
     * receptor debe seguir. Los frames se envían a ~25 Hz (cada 40ms).
     *
     * @param frame Frame con el estado actual del emisor.
     */
    suspend fun sendSync(frame: SyncFrame)

    /**
     * Flujo continuo de frames de sincronización recibidos del peer.
     *
     * El Flow emite un frame cada vez que se recibe uno del socket. El
     * receptor debe aplicar interpolación si hay pérdida de paquetes.
     *
     * @return Flow que emite SyncFrame a medida que llegan.
     */
    fun receiveSync(): Flow<SyncFrame>

    /**
     * Negocia el offset de reloj entre peers calculando el RTT.
     *
     * Envía un ping al peer, mide el tiempo de ida y vuelta, y calcula
     * el offset del clock remoto relativo al local.
     *
     * @return Offset del clock remoto en nanosegundos.
     *         Positivo si el remoto está adelantado, negativo si atrasado.
     */
    suspend fun negotiateClock(): Long
}
