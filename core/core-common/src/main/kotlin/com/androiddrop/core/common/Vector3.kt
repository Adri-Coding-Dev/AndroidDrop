package com.androiddrop.core.common

import kotlinx.serialization.Serializable
import kotlin.math.acos
import kotlin.math.sqrt

/**
 * Vector tridimensional para el motor de partículas y animaciones.
 *
 * POR QUÉ Vector3 propio vs librería externa: El motor de animación de AndroidDrop
 * necesita operaciones vectoriales simples (traslación, rotación, distancia entre
 * partículas) en el plano 3D de la esfera y portal. Crear nuestro propio tipo
 * evita dependencias pesadas (como physics engines) para operaciones que son
 * álgebra lineal básica. Además, los data class de Kotlin nos dan gratis
 * equals(), hashCode(), copy() y componentN().
 *
 * POR QUÉ Float vs Double: Las coordenadas de partículas y animaciones trabajan
 * en un espacio normalizado [-1, 1] donde Float (32 bits) tiene precisión más
 * que suficiente. Float es más eficiente en memoria (especialmente con ~2000
 * partículas) y tiene mejor rendimiento en GPUs si migramos a RenderScript/OpenGL.
 *
 * @property x Componente en el eje X (horizontal, derecha/izquierda).
 * @property y Componente en el eje Y (vertical, arriba/abajo).
 * @property z Componente en el eje Z (profundidad, hacia/desde el espectador).
 */
@Serializable
data class Vector3(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
) {
    companion object {
        /** Vector (0, 0, 0) - origen del sistema de coordenadas. */
        val zero = Vector3(0f, 0f, 0f)

        /** Vector (1, 1, 1) - vector unitario en todas las direcciones. */
        val one = Vector3(1f, 1f, 1f)

        /** Vector (0, 1, 0) - dirección hacia arriba en el espacio 3D. */
        val up = Vector3(0f, 1f, 0f)

        /** Vector (0, 0, 1) - dirección hacia adelante (profundidad positiva). */
        val forward = Vector3(0f, 0f, 1f)
    }

    // Operadores aritméticos ------------------------------------------------

    /**
     * Suma vectorial componente a componente.
     * Ej: (1,2,3) + (4,5,6) = (5,7,9)
     */
    operator fun plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)

    /**
     * Resta vectorial componente a componente.
     * Ej: (4,5,6) - (1,2,3) = (3,3,3)
     */
    operator fun minus(other: Vector3) = Vector3(x - other.x, y - other.y, z - other.z)

    /**
     * Multiplicación escalar (escala uniforme del vector).
     * Ej: (1,2,3) * 2 = (2,4,6)
     */
    operator fun times(scalar: Float) = Vector3(x * scalar, y * scalar, z * scalar)

    /**
     * División escalar.
     * Ej: (2,4,6) / 2 = (1,2,3)
     */
    operator fun div(scalar: Float) = Vector3(x / scalar, y / scalar, z / scalar)

    // Operaciones geométricas -----------------------------------------------

    /**
     * Magnitud (longitud) del vector: sqrt(x² + y² + z²).
     *
     * POR QUÉ se necesita: Calcular distancias entre partículas para
     * detectar colisiones, determinar fuerzas de atracción/repulsión,
     * y normalizar vectores de dirección.
     */
    fun length(): Float = sqrt(x * x + y * y + z * z)

    /**
     * Retorna un vector unitario (longitud = 1) en la misma dirección.
     *
     * POR QUÉ normalizar: Un vector normalizado preserva solo la dirección,
     * eliminando la magnitud. Esencial para calcular direcciones de movimiento
     * de partículas sin importar la velocidad actual.
     *
     * @throws ArithmeticException si el vector es [zero] (longitud = 0).
     */
    fun normalize(): Vector3 {
        val len = length()
        if (len == 0f) throw ArithmeticException("No se puede normalizar el vector cero")
        return this / len
    }

    /**
     * Producto punto (dot product): x1*x2 + y1*y2 + z1*z2.
     *
     * POR QUÉ dot product: Determina qué tan alineados están dos vectores.
     * Retorna 0 si son perpendiculares, >0 si apuntan en direcciones similares,
     * <0 si apuntan en direcciones opuestas. Fundamental para calcular ángulos
     * entre direcciones de movimiento y normales de superficie en la esfera.
     *
     * @param other El otro vector.
     * @return Escalar resultado del producto punto.
     */
    fun dot(other: Vector3): Float = x * other.x + y * other.y + z * other.z

    /**
     * Producto cruz (cross product): Vector perpendicular a ambos.
     *
     * POR QUÉ cross product: Genera un vector perpendicular al plano formado
     * por los dos vectores de entrada. Se usa para calcular normales de
     * superficie, direcciones de rotación orbital, y fuerzas perpendiculares
     * en simulaciones de partículas.
     *
     * Fórmula:
     *   x = y1*z2 - z1*y2
     *   y = z1*x2 - x1*z2
     *   z = x1*y2 - y1*x2
     *
     * @param other El otro vector.
     * @return Vector perpendicular a ambos (no normalizado).
     */
    fun cross(other: Vector3): Vector3 = Vector3(
        x = y * other.z - z * other.y,
        y = z * other.x - x * other.z,
        z = x * other.y - y * other.x
    )

    /**
     * Distancia euclidiana entre este vector y [other].
     *
     * POR QUÉ distanceTo vs (this - other).length(): Por claridad semántica
     * y eficiencia (evita crear un Vector3 temporal para la resta).
     *
     * @param other El otro punto/vector.
     * @return Distancia euclidiana (siempre >= 0).
     */
    fun distanceTo(other: Vector3): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}
