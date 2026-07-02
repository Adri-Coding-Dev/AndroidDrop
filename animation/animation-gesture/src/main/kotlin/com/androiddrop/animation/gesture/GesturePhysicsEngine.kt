package com.androiddrop.animation.gesture

import timber.log.Timber

/**
 * Motor de física para gestos táctiles con sensación elástica y natural.
 *
 * POR QUÉ física de resorte vs animaciones lineales: La interacción con la
 * esfera y el portal debe sentirse orgánica y tangible. Un modelo masa-resorte
 * (mass-spring-damper) proporciona la sensación elástica que el usuario espera
 * al arrastrar y soltar un objeto 3D. La curva de aceleración/deceleración
 * natural de un resorte es más agradable que una interpolación lineal.
 *
 * POR QUÉ historia de velocidad: Para calcular la velocidad de lanzamiento
 * (fling) con precisión, no basta con la velocidad instantánea. Guardamos un
 * historial de las últimas N posiciones y calculamos la velocidad media,
 * filtrando el ruido del dedo.
 *
 * @property springParams Parámetros por defecto del resorte.
 */
class GesturePhysicsEngine {

    /**
     * Parámetros del modelo masa-resorte-amortiguador.
     *
     * @property mass      Masa del objeto (1.0 = peso normal).
     * @property stiffness Rigidez del resorte (mayor = más rápido a la posición).
     * @property damping   Amortiguamiento (mayor = menos rebote, más suave).
     */
    data class SpringParams(
        val mass: Float = 1f,
        val stiffness: Float = 180f,
        val damping: Float = 15f
    )

    /** Límites del área de posicionamiento del portal. */
    data class Boundary2D(
        val minX: Float = -1f,
        val maxX: Float = 1f,
        val minY: Float = -1f,
        val maxY: Float = 1f
    )

    /** Límites actuales para el posicionamiento. */
    var boundary: Boundary2D = Boundary2D()

    /** Historial de timestamps para cálculo de velocidad de fling. */
    private val velocityHistory = mutableListOf<VelocitySample>()

    /** Ventana de tiempo para el historial de velocidad (200ms). */
    private companion object {
        private const val HISTORY_WINDOW_MS = 200L
        private const val MAX_HISTORY_SIZE = 20
    }

    /**
     * Muestra de velocidad con timestamp.
     */
    private data class VelocitySample(
        val position: Float,
        val timeMs: Long
    )

    /**
     * Actualiza la simulación de resorte para una dimensión.
     *
     * Implementa el método de Euler simplificado para el sistema:
     *   aceleración = (target - current) * stiffness / mass - velocity * damping / mass
     *   velocity += aceleración * deltaTime
     *   current += velocity * deltaTime
     *
     * @param current   Posición actual del objeto.
     * @param target    Posición objetivo a la que el resorte tira.
     * @param velocity  Velocidad actual del objeto.
     * @param params    Parámetros del resorte (masa, rigidez, amortiguamiento).
     * @param deltaTime Tiempo transcurrido en segundos.
     * @return Par (nueva posición, nueva velocidad).
     */
    fun updateSpring(
        current: Float,
        target: Float,
        velocity: Float,
        params: SpringParams,
        deltaTime: Float
    ): Pair<Float, Float> {
        // Fuerza del resorte: F = -k * (x - target)
        val springForce = -params.stiffness * (current - target)

        // Fuerza de amortiguamiento: F = -d * velocity
        val dampingForce = -params.damping * velocity

        // Aceleración: a = F / m
        val acceleration = (springForce + dampingForce) / params.mass

        // Integración de Euler
        val newVelocity = velocity + acceleration * deltaTime
        val newPosition = current + newVelocity * deltaTime

        return Pair(newPosition, newVelocity)
    }

    /**
     * Calcula la velocidad de lanzamiento basada en el historial de posiciones.
     *
     * Usa regresión lineal simple sobre las muestras del historial para
     * obtener una velocidad estable, filtrando el jitter del dedo.
     *
     * @param currentPosition Posición actual para añadir al historial.
     * @return Velocidad estimada en unidades/segundo.
     */
    fun calculateFlingVelocity(currentPosition: Float): Float {
        val now = System.currentTimeMillis()

        // Añadir muestra actual
        velocityHistory.add(VelocitySample(currentPosition, now))
        if (velocityHistory.size > MAX_HISTORY_SIZE) {
            velocityHistory.removeAt(0)
        }

        // Podar muestras fuera de la ventana de tiempo
        val cutoff = now - HISTORY_WINDOW_MS
        velocityHistory.removeAll { it.timeMs < cutoff }

        if (velocityHistory.size < 2) return 0f

        // Regresión lineal simple: velocity = pendiente de posición vs tiempo
        val n = velocityHistory.size
        var sumX = 0L
        var sumY = 0f
        var sumXY = 0f
        var sumX2 = 0L

        val firstTime = velocityHistory.first().timeMs

        for (sample in velocityHistory) {
            val x = sample.timeMs - firstTime // tiempo relativo
            val y = sample.position
            sumX += x
            sumY += y
            sumXY += x * y
            sumX2 += x * x
        }

        val nFloat = n.toFloat()
        val sumXFloat = sumX.toFloat()
        val denominator = nFloat * sumX2 - sumXFloat * sumXFloat

        if (denominator == 0f) return 0f

        // Pendiente = (n*sumXY - sumX*sumY) / (n*sumX2 - sumX^2)
        val slope = (nFloat * sumXY - sumXFloat * sumY) / denominator

        // Convertir a unidades/segundo (está en unidades/ms actualmente)
        return slope * 1000f
    }

    /**
     * Limita una posición dentro de los límites configurados.
     *
     * @param position Posición a limitar.
     * @return Posición ajustada dentro de los límites.
     */
    fun clampToBoundary(position: Pair<Float, Float>): Pair<Float, Float> {
        return Pair(
            position.first.coerceIn(boundary.minX, boundary.maxX),
            position.second.coerceIn(boundary.minY, boundary.maxY)
        )
    }

    /**
     * Limpia el historial de velocidad (útil al iniciar un nuevo gesto).
     */
    fun resetVelocityHistory() {
        velocityHistory.clear()
    }
}
