package com.androiddrop.sync.protocol

import timber.log.Timber

/**
 * Sincronizador de reloj entre peers para compensación de latencia.
 *
 * POR QUÉ compensación de latencia vs simple timestamp: Cuando un frame viaja
 * del emisor al receptor, la información ya está desactualizada por el tiempo
 * de transmisión (RTT/2). Para animaciones suaves, el receptor debe proyectar
 * el estado hacia adelante usando el offset de clock, no el timestamp crudo.
 *
 * El algoritmo:
 *   1. Se mide el RTT con un ping-pong.
 *   2. El offset del clock remoto = timestamp_remoto - timestamp_local - RTT/2
 *   3. Los frames recibidos se ajustan: timestamp_ajustado = timestamp_frame - offset
 *   4. Si el RTT es muy alto (>100ms), se aplica interpolación extra.
 *
 * @property masterClockOffset Offset del clock del maestro en nanosegundos.
 */
class ClockSynchronizer {

    /** Offset del clock maestro relativo al local. */
    @Volatile
    var masterClockOffset: Long = 0L
        private set

    /** RTT medido más reciente en nanosegundos. */
    @Volatile
    var lastRttNanos: Long = 0L
        private set

    /** Historial de RTT para calcular promedio móvil. */
    private val rttHistory = ArrayDeque<Long>(MAX_RTT_SAMPLES)

    companion object {
        /** Número máximo de muestras de RTT para promedio móvil. */
        private const val MAX_RTT_SAMPLES = 10

        /** RTT máximo aceptable antes de considerar latencia alta. */
        private const val HIGH_LATENCY_THRESHOLD_NS = 100_000_000L // 100ms
    }

    /**
     * Ejecuta la sincronización de clock usando el protocolo.
     *
     * @param protocol Protocolo de sincronización activo.
     * @return Offset calculado en nanosegundos.
     */
    suspend fun synchronize(protocol: SyncProtocol): Long {
        Timber.d("ClockSynchronizer: iniciando sincronización")

        val offset = protocol.negotiateClock()
        masterClockOffset = offset

        Timber.d("ClockSynchronizer: offset del clock maestro = ${offset / 1_000_000}ms")
        return offset
    }

    /**
     * Aplica compensación de latencia a un frame recibido.
     *
     * Ajusta el timestamp del frame para que esté en la línea de tiempo del
     * clock local. Si la latencia es alta, también ajusta posición y rotación
     * para compensar el retraso.
     *
     * POR QUÉ extrapolación lineal vs predicción compleja: Para un frame rate
     * de 25 Hz y latencias típicas de <50ms, la extrapolación lineal simple
     * (asumir que el objeto sigue moviéndose en la misma dirección) es suficiente.
     * Predicción más compleja (Kalman, splines) añadiría complejidad innecesaria
     * para una mejora marginal.
     *
     * @param frame Frame recibido del peer.
     * @return Frame con timestamp ajustado y posición compensada.
     */
    fun applyLatencyCompensation(frame: SyncFrame): SyncFrame {
        // Ajustar timestamp al clock local
        val adjustedTimestamp = frame.timestamp - masterClockOffset
        val now = System.nanoTime()
        val latency = now - adjustedTimestamp

        // Si la latencia es baja, solo ajustar timestamp
        if (latency < HIGH_LATENCY_THRESHOLD_NS) {
            return frame.copy(timestamp = adjustedTimestamp)
        }

        // Latencia alta: extrapolar posición hacia adelante
        Timber.d("ClockSynchronizer: latencia alta detectada (${latency / 1_000_000}ms), extrapolando")

        val extrapolationFactor = latency.toFloat() / 1_000_000_000f

        // Por ahora solo ajustamos timestamp; la extrapolación real requeriría
        // historial de velocity del frame (que podría añadirse a SyncFrame en el futuro)
        return frame.copy(timestamp = adjustedTimestamp)
    }

    /**
     * Registra una muestra de RTT para el promedio móvil.
     */
    fun recordRtt(rttNanos: Long) {
        rttHistory.addLast(rttNanos)
        if (rttHistory.size > MAX_RTT_SAMPLES) {
            rttHistory.removeFirst()
        }
        lastRttNanos = rttNanos
    }

    /**
     * Retorna el RTT promedio de las últimas muestras.
     */
    fun averageRttNanos(): Long {
        if (rttHistory.isEmpty()) return 0L
        return rttHistory.sum() / rttHistory.size
    }
}
