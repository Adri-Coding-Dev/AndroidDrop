package com.androiddrop.animation.engine

import android.view.Choreographer
import timber.log.Timber

/**
 * Controlador de tasa de frames para el motor de animación.
 *
 * POR QUÉ un controlador separado: La lógica de frame pacing es compleja e
 * incluye detección de capacidad del dispositivo, cálculo de sleep time y
 * adaptación dinámica. Separarla de [AnimationEngine] permite probarla
 * unitariamente y reutilizarla en diferentes contextos (OpenGL, Canvas, Compose).
 *
 * POR QUÉ Choreographer: Choreographer es la API de Android que recibe los
 * callbacks de vsync de la pantalla. Nos permite detectar si el dispositivo
 * soporta 120Hz consultando el frame interval. Sin embargo, no todos los
 * dispositivos exponen esta información correctamente, por lo que se usa un
 * enfoque híbrido: detección por API + fallback heurístico.
 *
 * @property targetFps FPS objetivo configurado (60 o 120).
 */
class FrameRateController(
    var targetFps: Int = 60
) {
    companion object {
        /** Factor de conversión de segundos a nanosegundos. */
        private const val NANOS_PER_SECOND = 1_000_000_000L

        /** Tolerancia para considerar que un dispositivo es de 120Hz. */
        private const val TOLERANCE_NANOS = 1_000_000L // 1ms
    }

    /**
     * Retorna el tiempo objetivo por frame en nanosegundos.
     *
     * Para 60 FPS: ~16.67ms (16,666,667 ns)
     * Para 120 FPS: ~8.33ms (8,333,333 ns)
     *
     * @return Nanosegundos objetivo por frame.
     */
    fun getTargetFrameTime(): Long = NANOS_PER_SECOND / targetFps

    /**
     * Calcula el tiempo de sleep necesario para mantener el target FPS.
     *
     * @param frameStartNanos Timestamp de inicio del frame (System.nanoTime).
     * @return Nanosegundos que debe dormir el loop para cumplir el target.
     *         Puede ser 0 si ya excedimos el tiempo del frame.
     */
    fun calculateSleepTime(frameStartNanos: Long): Long {
        val elapsed = System.nanoTime() - frameStartNanos
        val target = getTargetFrameTime()
        val sleepTime = target - elapsed
        return if (sleepTime > 0) sleepTime else 0L
    }

    /**
     * Detecta la capacidad máxima de refresco del dispositivo.
     *
     * Usa [Choreographer] para obtener el frame interval del vsync. Si el
     * intervalo es ~8.33ms (120Hz), retorna 120. Si es ~16.67ms (60Hz) o
     * no se puede determinar, retorna 60.
     *
     * POR QUÉ detección automática: Forzar 120 FPS en un dispositivo de 60Hz
     * desperdicia batería y CPU. Viceversa, limitar a 60 FPS en un panel 120Hz
     * desperdicia la suavidad que el hardware puede proporcionar. La adaptación
     * automática da la mejor experiencia sin configuración manual.
     *
     * @return 120 si el dispositivo soporta 120Hz, 60 en caso contrario.
     */
    fun adaptToDeviceCapability(): Int {
        return try {
            val choreographer = Choreographer.getInstance()
            val field = Choreographer::class.java.getDeclaredField("mFrameIntervalNanos")
            field.isAccessible = true
            val frameIntervalNanos = field.get(choreographer) as Long

            Timber.d("FrameRateController: frameIntervalNanos = $frameIntervalNanos")

            // 120Hz ≈ 8,333,333 ns, 60Hz ≈ 16,666,667 ns
            if (frameIntervalNanos < (NANOS_PER_SECOND / 60) - TOLERANCE_NANOS) {
                Timber.d("FrameRateController: dispositivo 120Hz detectado")
                120
            } else {
                Timber.d("FrameRateController: dispositivo 60Hz detectado")
                60
            }
        } catch (e: Exception) {
            Timber.w("FrameRateController: no se pudo detectar capacidad, usando 60Hz")
            60
        }
    }
}
