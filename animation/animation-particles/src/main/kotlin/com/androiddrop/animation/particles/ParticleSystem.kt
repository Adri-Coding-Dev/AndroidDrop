package com.androiddrop.animation.particles

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import com.androiddrop.core.common.Vector3
import kotlin.math.cos
import kotlin.math.sin
import timber.log.Timber

/**
 * Sistema de partículas con pool reciclable y múltiples emisores.
 *
 * POR QUÉ pool de partículas: Crear y destruir ~2000 objetos Particle por
 * segundo dispara el GC de Kotlin/Android causando micro-stutters visibles
 * en la animación. El pool pre-asigna [maxParticles] instancias y las
 * recicla: cuando una partícula muere, vuelve al pool en lugar de ser
 * recolectada. Esto elimina por completo la asignación en el heap durante
 * el gameplay.
 *
 * POR QUÉ Canvas vs OpenGL para partículas: Las partículas de AndroidDrop
 * son pequeños puntos/círculos con alpha blending. Canvas (con Paint y
 * hardware acceleration) maneja esto eficientemente sin la complejidad de
 * un sistema de partículas basado en shaders. Para los ~2000 particles
 * de este sistema, Canvas rinde a 60+ FPS sin problemas.
 *
 * @property maxParticles Número máximo de partículas activas simultáneamente.
 * @property emitterType  Tipo de emisor por defecto.
 */
class ParticleSystem(
    val maxParticles: Int = 2000,
    val emitterType: EmitterType = EmitterType.RADIAL
) {
    /** Pool de partículas vivas (índices en uso). */
    private val activeParticles = mutableListOf<Particle>()

    /** Pool de partículas muertas disponibles para reciclaje. */
    private val deadPool = mutableListOf<Particle>()

    /** Lista de emisores activos. */
    private val emitters = mutableListOf<ParticleEmitter>()

    /** Paint reutilizado para renderizado (evita crear objetos por frame). */
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    /** Factor de amortiguamiento global (0.0 - 1.0). */
    var damping: Float = 0.98f

    /** Gravedad global aplicada a todas las partículas. */
    var gravity: Vector3 = Vector3(0f, -9.8f, 0f)

    /**
     * Inicializa el pool con [maxParticles] instancias reciclables.
     */
    init {
        for (i in 0 until maxParticles) {
            deadPool.add(Particle())
        }
        Timber.d("ParticleSystem: pool inicializado con $maxParticles partículas")
    }

    /**
     * Añade un emisor al sistema.
     */
    fun addEmitter(emitter: ParticleEmitter) {
        emitters.add(emitter)
    }

    /**
     * Elimina un emisor del sistema.
     */
    fun removeEmitter(emitter: ParticleEmitter) {
        emitters.remove(emitter)
    }

    /**
     * Actualiza la física de todas las partículas activas.
     *
     * @param deltaTime Tiempo transcurrido en segundos.
     */
    fun update(deltaTime: Float) {
        // Emitir nuevas partículas desde todos los emisores
        for (emitter in emitters) {
            val newParticles = emitter.emit(deltaTime)
            for (particle in newParticles) {
                spawnParticle(particle)
            }
        }

        // Actualizar partículas activas
        val iterator = activeParticles.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()

            // Reducir vida
            particle.life -= deltaTime

            if (particle.isDead) {
                // Reciclar: devolver al pool de muertos
                particle.reset()
                deadPool.add(particle)
                iterator.remove()
                continue
            }

            // Actualizar física
            particle.velocity += gravity * deltaTime
            particle.velocity = particle.velocity * damping
            particle.position += particle.velocity * deltaTime

            // Actualizar rotación
            particle.rotation += particle.angularVelocity * deltaTime

            // Actualizar alpha basado en vida restante
            particle.alpha = particle.lifeFraction
        }
    }

    /**
     * Renderiza todas las partículas activas en un [Canvas].
     *
     * @param canvas Canvas de Compose o View donde dibujar.
     */
    fun render(canvas: Canvas) {
        // Ordenar por profundidad (Z) para alpha blending correcto
        val sorted = activeParticles.sortedByDescending { it.position.z }

        for (particle in sorted) {
            paint.alpha = (particle.alpha * 255).toInt().coerceIn(0, 255)
            paint.color = particle.color

            // Crear gradiente radial para efecto de glow
            val radius = particle.size / 2f
            paint.shader = RadialGradient(
                particle.position.x, particle.position.y,
                radius,
                intArrayOf(
                    (particle.color and 0x00FFFFFF) or ((particle.alpha * 255).toInt() shl 24),
                    0x00000000
                ),
                floatArrayOf(0.3f, 1.0f),
                Shader.TileMode.CLAMP
            )

            canvas.save()
            canvas.rotate(particle.rotation, particle.position.x, particle.position.y)
            canvas.drawCircle(particle.position.x, particle.position.y, radius, paint)
            canvas.restore()

            paint.shader = null
        }
    }

    /**
     * Activa una partícula del pool de muertos o la crea si es posible.
     *
     * @param particle Partícula configurada para activar.
     * @return true si se pudo activar, false si el pool está lleno.
     */
    private fun spawnParticle(particle: Particle): Boolean {
        if (activeParticles.size >= maxParticles) {
            return false
        }

        val recycled = if (deadPool.isNotEmpty()) {
            deadPool.removeAt(deadPool.size - 1).also {
                it.position = particle.position
                it.velocity = particle.velocity
                it.life = particle.life
                it.maxLife = particle.maxLife
                it.size = particle.size
                it.color = particle.color
                it.alpha = particle.alpha
                it.rotation = particle.rotation
                it.angularVelocity = particle.angularVelocity
            }
        } else {
            particle.copy()
        }

        activeParticles.add(recycled)
        return true
    }

    /**
     * Retorna el número actual de partículas activas.
     */
    val activeCount: Int
        get() = activeParticles.size

    /**
     * Limpia todas las partículas activas (las devuelve al pool).
     */
    fun clear() {
        for (particle in activeParticles) {
            particle.reset()
            deadPool.add(particle)
        }
        activeParticles.clear()
    }
}
