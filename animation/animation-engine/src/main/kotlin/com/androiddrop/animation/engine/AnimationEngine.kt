package com.androiddrop.animation.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Motor principal de animación basado en un loop de corrutina con target FPS.
 *
 * POR QUÉ un loop de corrutina vs Handler/Runnable: Las corrutinas proporcionan
 * cancelación cooperativa, manejo de excepciones estructurado, y se integran
 * naturalmente con el lifecycle de Compose via [CoroutineScope]. Además,
 * `delay()` es no-bloqueante y preciso para el frame pacing.
 *
 * POR QUÉ deltaTime vs frame rate fijo: En dispositivos con tasas de refresco
 * variables (120Hz en flagships, 60Hz en gama media), calcular el delta entre
 * frames permite que las animaciones corran a la misma velocidad independientemente
 * del FPS real.
 *
 * El motor mide el drift acumulado y ajusta el sleep del siguiente frame para
 * mantener el target FPS promedio, evitando tanto el sobrecalentamiento (frames
 * innecesarios) como la sensación de lag (frames perdidos).
 *
 * @property frameRate Frames por segundo objetivo (60 o 120). Define el delay
 *                     base entre frames: ceil(1000 / frameRate) ms.
 * @property isRunning Indica si el loop de frames está actualmente activo.
 * @property onFrame Callback invocado en cada frame con el [deltaTime] en segundos.
 *                   Se ejecuta en el contexto de la corrutina del motor.
 */
class AnimationEngine(
    var frameRate: Int = 60
) {
    /** Indica si el loop de frames está corriendo. */
    @Volatile
    var isRunning: Boolean = false
        private set

    /** Callback de frame: recibe deltaTime en segundos. */
    var onFrame: ((deltaTime: Float) -> Unit)? = null

    /** Job interno que mantiene la referencia a la corrutina del loop. */
    private var frameJob: Job? = null

    /** Timestamp del frame anterior en nanosegundos (System.nanoTime). */
    private var lastFrameNanos: Long = 0L

    /** Drift acumulado para corrección de frame pacing. */
    private var accumulatedDrift: Long = 0L

    /** Número de frames contados en el período de medición. */
    private var frameCount: Int = 0

    /** Tiempo acumulado de frames en el período de medición. */
    private var measurementPeriodNanos: Long = 0L

    companion object {
        /** FPS alto para dispositivos flagship con pantalla 120Hz. */
        const val FPS_120 = 120

        /** FPS estándar para la mayoría de dispositivos. */
        const val FPS_60 = 60

        /** Tolerancia máxima de drift antes de forzar reinicio (5ms). */
        private const val MAX_DRIFT_TOLERANCE_NANOS = 5_000_000L

        /** Período de reporte de FPS real en nanosegundos (cada 2 segundos). */
        private const val REPORT_INTERVAL_NANOS = 2_000_000_000L
    }

    /**
     * Inicia el loop de animación.
     *
     * Crea una corrutina dentro del [scope] proporcionado que ejecuta el
     * loop frame a frame. Cada iteración:
     *   1. Calcula [deltaTime] basado en el tiempo real transcurrido.
     *   2. Invoca [onFrame] con el delta.
     *   3. Calcula el sleep necesario para mantener el target FPS.
     *   4. Ajusta por drift acumulado para mantener precisión a largo plazo.
     *
     * @param scope CoroutineScope donde se lanzará el loop. Normalmente
     *              el scope de un viewModel o un lifecycleOwner.
     */
    fun startFrameLoop(scope: CoroutineScope) {
        if (isRunning) {
            Timber.w("AnimationEngine: el loop ya está corriendo")
            return
        }

        isRunning = true
        lastFrameNanos = System.nanoTime()
        accumulatedDrift = 0L

        frameJob = scope.launch {
            Timber.d("AnimationEngine: loop iniciado a $frameRate FPS")

            while (isActive && isRunning) {
                val frameStartNanos = System.nanoTime()

                // Calcular deltaTime en segundos a partir del tiempo real
                val deltaNanos = frameStartNanos - lastFrameNanos
                val deltaTime = deltaNanos.toFloat() / 1_000_000_000f

                // Actualizar timestamp para el próximo frame
                lastFrameNanos = frameStartNanos

                // Invocar callback de frame
                onFrame?.invoke(deltaTime)

                // Calcular cuánto debemos dormir para mantener el target FPS
                val targetFrameTime = getTargetFrameTime()
                val elapsedNanos = System.nanoTime() - frameStartNanos
                var sleepTimeNanos = targetFrameTime - elapsedNanos + accumulatedDrift

                // Limitar sleep máximo para evitar pausas largas
                if (sleepTimeNanos > targetFrameTime) {
                    sleepTimeNanos = targetFrameTime
                }

                // Si el drift es muy grande, lo corregimos gradualmente
                if (sleepTimeNanos < 0) {
                    accumulatedDrift = sleepTimeNanos // drift negativo = vamos lentos
                    sleepTimeNanos = 0
                } else {
                    accumulatedDrift = 0L
                }

                // Dormir el tiempo calculado (en milisegundos)
                if (sleepTimeNanos > 0) {
                    delay(sleepTimeNanos / 1_000_000L)
                }

                // Actualizar métricas de rendimiento cada REPORT_INTERVAL
                measurementPeriodNanos += System.nanoTime() - frameStartNanos
                frameCount++

                if (measurementPeriodNanos >= REPORT_INTERVAL_NANOS) {
                    val actualFps = frameCount.toFloat() / (measurementPeriodNanos / 1_000_000_000f)
                    Timber.d("AnimationEngine: FPS real ~%.1f (target: %d)".format(actualFps, frameRate))
                    frameCount = 0
                    measurementPeriodNanos = 0L
                }
            }

            Timber.d("AnimationEngine: loop terminado")
            isRunning = false
        }
    }

    /**
     * Detiene el loop de animación.
     *
     * Cancela la corrutina del loop y espera su finalización. Es seguro
     * llamar a stop() múltiples veces.
     */
    fun stop() {
        isRunning = false
        frameJob?.cancel(null)
        frameJob = null
        Timber.d("AnimationEngine: loop detenido")
    }

    /**
     * Calcula el deltaTime manualmente entre dos puntos de medición.
     *
     * Útil para cálculos fuera del loop principal, por ejemplo en
     * [RenderNode.update] cuando se necesita el delta para físicas.
     *
     * @return Delta de tiempo en segundos desde la última llamada a
     *         [calculateDeltaTime] o desde el inicio del motor.
     */
    fun calculateDeltaTime(): Float {
        val now = System.nanoTime()
        val delta = now - lastFrameNanos
        lastFrameNanos = now
        return delta.toFloat() / 1_000_000_000f
    }

    /**
     * Retorna el tiempo objetivo por frame en nanosegundos.
     *
     * @return Duración objetivo de cada frame basada en [frameRate].
     */
    private fun getTargetFrameTime(): Long = 1_000_000_000L / frameRate
}
