package com.androiddrop.animation.particles

import com.androiddrop.core.common.Vector3

/**
 * Partícula individual dentro del sistema de partículas.
 *
 * POR QUÉ data class con vars: Cada partícula se actualiza en cada frame
 * (posición, velocidad, vida, etc.). Usar var en lugar de copy() evita
 * la creación masiva de objetos en el heap, reduciendo la presión del GC.
 * El pool de partículas reciclables ([ParticleSystem]) reutiliza instancias
 * en lugar de crear nuevas, eliminando por completo la asignación durante
 * el juego.
 *
 * @property position      Posición 3D actual de la partícula.
 * @property velocity      Velocidad actual en unidades/segundo.
 * @property life          Vida restante de la partícula en segundos.
 * @property maxLife       Vida máxima inicial (para calcular alpha por edad).
 * @property size          Tamaño visual de la partícula en píxeles.
 * @property color         Color ARGB de la partícula (0xAARRGGBB).
 * @property alpha         Opacidad actual (0.0 - 1.0).
 * @property rotation      Rotación actual en grados.
 * @property angularVelocity Velocidad angular en grados/segundo.
 */
data class Particle(
    var position: Vector3 = Vector3.zero,
    var velocity: Vector3 = Vector3.zero,
    var life: Float = 0f,
    var maxLife: Float = 0f,
    var size: Float = 4f,
    var color: Int = 0xFFFFFFFF.toInt(),
    var alpha: Float = 1f,
    var rotation: Float = 0f,
    var angularVelocity: Float = 0f
) {
    /**
     * Retorna la fracción de vida restante (0.0 - 1.0).
     * 1.0 = recién creada, 0.0 = a punto de morir.
     */
    val lifeFraction: Float
        get() = if (maxLife > 0f) (life / maxLife).coerceIn(0f, 1f) else 0f

    /**
     * Indica si la partícula está muerta (life <= 0).
     */
    val isDead: Boolean
        get() = life <= 0f

    /**
     * Resetea la partícula para reutilización en el pool.
     */
    fun reset() {
        position = Vector3.zero
        velocity = Vector3.zero
        life = 0f
        maxLife = 0f
        size = 4f
        color = 0xFFFFFFFF.toInt()
        alpha = 1f
        rotation = 0f
        angularVelocity = 0f
    }
}
