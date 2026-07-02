package com.androiddrop.sync.protocol

import com.androiddrop.core.common.Vector3
import timber.log.Timber

/**
 * Motor de interpolación entre frames de sincronización.
 *
 * POR QUÉ interpolación de frames perdidos: En una red peer-topeer, los
 * paquetes pueden perderse o llegar fuera de orden. En lugar de saltos
 * bruscos en la animación, interpolamos suavemente entre el frame anterior
 * y el siguiente. La interpolación lineal es aceptable a 25 Hz porque el
 * intervalo entre frames (40ms) es menor que la persistencia visual humana
 * (~100ms para movimiento suave).
 *
 * Buffer circular: Mantiene los últimos N frames para poder interpolar
 * hacia atrás si un frame llega tarde pero dentro de la ventana.
 */
class InterpolationEngine {

    /** Buffer circular de frames históricos. */
    private val frameHistory = mutableListOf<SyncFrame>()

    /** Número máximo de frames en el historial. */
    private var maxHistorySize: Int = 10

    companion object {
        /** Ventana máxima para interpolar en nanosegundos (200ms). */
        private const val MAX_INTERPOLATION_WINDOW_NS = 200_000_000L
    }

    /**
     * Configura el tamaño máximo del buffer circular.
     */
    fun setMaxHistorySize(size: Int) {
        maxHistorySize = size.coerceAtLeast(2)
        while (frameHistory.size > maxHistorySize) {
            frameHistory.removeAt(0)
        }
    }

    /**
     * Añade un frame al buffer histórico.
     *
     * Los frames se mantienen ordenados por timestamp. Si el buffer excede
     * [maxHistorySize], se descartan los frames más antiguos.
     *
     * @param frame Frame recibido para añadir al historial.
     */
    fun addFrame(frame: SyncFrame) {
        // Insertar ordenado por timestamp
        val index = frameHistory.indexOfFirst { it.timestamp > frame.timestamp }
        if (index < 0) {
            frameHistory.add(frame)
        } else {
            frameHistory.add(index, frame)
        }

        // Purgar frames antiguos
        while (frameHistory.size > maxHistorySize) {
            frameHistory.removeAt(0)
        }

        Timber.v("InterpolationEngine: buffer = ${frameHistory.size} frames")
    }

    /**
     * Interpola el estado en un timestamp específico.
     *
     * Busca los dos frames que rodean [timestamp] y realiza interpolación
     * lineal entre ellos. Si el timestamp está fuera del rango del buffer,
     * retorna el frame más cercano (extrapolación = hold).
     *
     * @param timestamp Timestamp objetivo en la línea de tiempo local.
     * @return Frame interpolado en [timestamp], o null si no hay suficientes frames.
     */
    fun interpolateFrame(timestamp: Long): SyncFrame? {
        if (frameHistory.isEmpty()) return null
        if (frameHistory.size == 1) return frameHistory.first()

        // Encontrar el frame anterior al timestamp
        val prevIndex = frameHistory.indexOfLast { it.timestamp <= timestamp }

        if (prevIndex < 0) {
            // Timestamp anterior al frame más antiguo
            return frameHistory.first()
        }

        if (prevIndex >= frameHistory.size - 1) {
            // Timestamp posterior al último frame
            return frameHistory.last()
        }

        val prev = frameHistory[prevIndex]
        val next = frameHistory[prevIndex + 1]

        // Verificar que los frames están dentro de la ventana de interpolación
        val delta = next.timestamp - prev.timestamp
        if (delta > MAX_INTERPOLATION_WINDOW_NS) {
            Timber.d("InterpolationEngine: gap muy grande (${delta / 1_000_000}ms), saltando interpolación")
            return prev
        }

        // Calcular factor de interpolación (0.0 = prev, 1.0 = next)
        val t = ((timestamp - prev.timestamp).toFloat() / delta).coerceIn(0f, 1f)

        return interpolateLinear(prev, next, t)
    }

    /**
     * Interpola linealmente entre dos frames.
     *
     * @param a Frame anterior.
     * @param b Frame siguiente.
     * @param t Factor de interpolación (0.0 = a, 1.0 = b).
     * @return Frame interpolado.
     */
    private fun interpolateLinear(a: SyncFrame, b: SyncFrame, t: Float): SyncFrame {
        return SyncFrame(
            timestamp = (a.timestamp + ((b.timestamp - a.timestamp) * t.toLong()).toLong()).coerceAtLeast(0),
            position = Vector3(
                lerp(a.position.x, b.position.x, t),
                lerp(a.position.y, b.position.y, t),
                lerp(a.position.z, b.position.z, t)
            ),
            rotation = Vector3(
                lerpAngle(a.rotation.x, b.rotation.x, t),
                lerpAngle(a.rotation.y, b.rotation.y, t),
                lerpAngle(a.rotation.z, b.rotation.z, t)
            ),
            scale = lerp(a.scale, b.scale, t),
            energy = lerp(a.energy, b.energy, t),
            phase = if (t < 0.5f) a.phase else b.phase, // fase no interpolable
            sequenceNumber = a.sequenceNumber,
            isKeyFrame = false // frame interpolado, no clave
        )
    }

    /**
     * Interpolación lineal de floats.
     */
    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

    /**
     * Interpolación lineal de ángulos con wrap-around.
     *
     * POR QUÉ lerpAngle vs lerp normal: Los ángulos tienen discontinuidad en
     * 0/360 grados. Si un frame tiene rotación 350° y el siguiente 10°,
     * la interpolación lineal pasaría por 180° en lugar de por 0°.
     * lerpAngle calcula la diferencia más corta alrededor del círculo.
     */
    private fun lerpAngle(a: Float, b: Float, t: Float): Float {
        var diff = b - a
        while (diff > 180f) diff -= 360f
        while (diff < -180f) diff += 360f
        return a + diff * t
    }

    /**
     * Limpia el buffer de frames.
     */
    fun clear() {
        frameHistory.clear()
    }

    /**
     * Retorna el número de frames en el buffer.
     */
    val size: Int
        get() = frameHistory.size
}
