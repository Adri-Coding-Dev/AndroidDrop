package com.androiddrop.animation.particles

import com.androiddrop.core.common.Vector3
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Tipo de emisor de partículas.
 *
 * POR QUÉ diferentes tipos de emisor: Cada estado de la esfera y el portal
 * requiere un patrón de emisión diferente. La esfera en estado IDLE emite
 * partículas desde su superficie (SPHERE_SURFACE), mientras que el portal
 * las emite desde el centro del vórtice (POINT o RADIAL).
 */
enum class EmitterType {
    /** Emisión desde un punto: todas las partículas nacen en [position]. */
    POINT,

    /** Emisión radial: partículas emergen en un radio alrededor de [position]. */
    RADIAL,

    /** Emisión en cono: partículas dentro de un cono con dirección y apertura. */
    CONE,

    /** Emisión desde la superficie de una esfera: partículas en la cáscara. */
    SPHERE_SURFACE
}

/**
 * Emisor de partículas configurable.
 *
 * POR QUÉ un emisor separado de [ParticleSystem]: Separar la generación
 * de la simulación permite tener múltiples emisores con diferentes
 * configuraciones (posición, rate, colores) alimentando el mismo sistema
 * de partículas. Esto es necesario para la esfera (emisor superficial) y
 * el portal (emisor puntual) simultáneamente.
 *
 * @property type     Tipo de emisión (puntual, radial, cono, esférica).
 * @property position Posición del emisor en el espacio 3D.
 * @property rate     Número de partículas por segundo.
 */
class ParticleEmitter(
    val type: EmitterType,
    val position: Vector3,
    val rate: Int
) {
    /** Dirección base de emisión (por defecto hacia arriba). */
    var direction: Vector3 = Vector3.up

    /** Dispersión angular en grados (0 = dirección exacta, 180 = todas direcciones). */
    var spread: Float = 45f

    /** Velocidad inicial mínima y máxima de las partículas. */
    var speedMin: Float = 10f
    var speedMax: Float = 50f

    /** Rango de vida de las partículas (segundos). */
    var lifeMin: Float = 0.5f
    var lifeMax: Float = 2.0f

    /** Rango de tamaño inicial (píxeles). */
    var sizeMin: Float = 2f
    var sizeMax: Float = 8f

    /** Colores posibles para las partículas (se elige aleatoriamente). */
    var colors: List<Int> = listOf(0xFFFFFFFF.toInt())

    /** Acumulador de tiempo para emitir al rate correcto. */
    private var accumulator: Float = 0f

    /** Generador de números aleatorios para emisión. */
    private val random: Random = Random

    /**
     * Genera partículas según el rate y el deltaTime.
     *
     * @param deltaTime Tiempo transcurrido desde el último frame.
     * @return Lista de nuevas partículas a añadir al sistema.
     */
    fun emit(deltaTime: Float): List<Particle> {
        accumulator += deltaTime
        val particlesToEmit = (accumulator * rate).toInt()
        accumulator -= particlesToEmit.toFloat() / rate

        if (particlesToEmit <= 0) return emptyList()

        val particles = mutableListOf<Particle>()

        for (i in 0 until particlesToEmit) {
            val particle = Particle()

            // Posición según tipo de emisor
            particle.position = when (type) {
                EmitterType.POINT -> position
                EmitterType.RADIAL -> {
                    val radius = random.nextFloat() * 0.5f
                    val angle = random.nextFloat() * 2f * Math.PI.toFloat()
                    Vector3(
                        position.x + cos(angle) * radius,
                        position.y,
                        position.z + sin(angle) * radius
                    )
                }
                EmitterType.CONE -> {
                    val angle = random.nextFloat() * spread * (Math.PI.toFloat() / 180f)
                    val rotAngle = random.nextFloat() * 2f * Math.PI.toFloat()
                    val dir = Vector3(
                        sin(angle) * cos(rotAngle),
                        cos(angle),
                        sin(angle) * sin(rotAngle)
                    )
                    position + dir * 0.1f
                }
                EmitterType.SPHERE_SURFACE -> {
                    val theta = random.nextFloat() * 2f * Math.PI.toFloat()
                    val phi = kotlin.math.acos(2f * random.nextFloat() - 1f)
                    Vector3(
                        position.x + sin(phi) * cos(theta),
                        position.y + sin(phi) * sin(theta),
                        position.z + cos(phi)
                    )
                }
            }

            // Velocidad inicial
            val speed = speedMin + random.nextFloat() * (speedMax - speedMin)
            particle.velocity = when (type) {
                EmitterType.SPHERE_SURFACE -> {
                    // Desde la superficie: normal hacia afuera
                    (particle.position - position).normalize() * speed
                }
                else -> {
                    val spreadRad = spread * (Math.PI.toFloat() / 180f)
                    val theta = random.nextFloat() * 2f * Math.PI.toFloat()
                    val phi = random.nextFloat() * spreadRad
                    val dir = Vector3(
                        sin(phi) * cos(theta),
                        cos(phi),
                        sin(phi) * sin(theta)
                    )
                    // Rotar dirección base hacia el "up" del emisor
                    if (type == EmitterType.CONE) {
                        dir * speed
                    } else {
                        (direction + dir * 0.3f).normalize() * speed
                    }
                }
            }

            // Vida, tamaño y color
            particle.maxLife = lifeMin + random.nextFloat() * (lifeMax - lifeMin)
            particle.life = particle.maxLife
            particle.size = sizeMin + random.nextFloat() * (sizeMax - sizeMin)
            particle.color = colors[random.nextInt(colors.size)]

            particles.add(particle)
        }

        return particles
    }
}
