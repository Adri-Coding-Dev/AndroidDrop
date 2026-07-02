package com.androiddrop.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.androiddrop.core.common.Constants.MAX_PARTICLES
import com.androiddrop.core.ui.theme.AndroidDropColorScheme
import com.androiddrop.core.ui.theme.LocalAndroidDropColorScheme
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Fondo animado con partículas de energía.
 *
 * POR QUÉ partículas animadas en el fondo: AndroidDrop no es solo una
 * herramienta, sino una experiencia. El fondo con partículas crea la
 * atmósfera de "energía contenida": los datos fluyen como partículas
 * subatómicas en un campo controlado. Las partículas se mueven lentamente
 * en órbitas elípticas, simulando electrones alrededor de un núcleo.
 *
 * POR QUÉ Canvas vs Lottie/OpenGL: Canvas es parte de Compose, no requiere
 * dependencias externas, y para ~30 partículas con movimiento simple es más
 * que suficiente. OpenGL sería overkill y Lottie no permitiría partículas
 * procedurales que se adaptan al tamaño de la pantalla.
 *
 * Las partículas:
 *   - Se mueven en órbitas lentas alrededor del centro
 *   - Tienen colores energy (verde, cian, ámbar) con alpha variable
 *   - Cada partícula tiene un tamaño y velocidad únicos
 *   - El límite [MAX_PARTICLES] evita problemas de rendimiento
 *
 * @param modifier Modificador para el tamaño del canvas.
 */
@Composable
fun AnimatedBackground(
    modifier: Modifier = Modifier
) {
    val colors = LocalAndroidDropColorScheme.current

    val infiniteTransition = rememberInfiniteTransition(label = "bgParticles")

    // Tiempo global de animación (ciclo continuo)
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Generar partículas con parámetros aleatorios (una vez, no por frame)
    val particles = rememberParticles()

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.width.coerceAtMost(size.height) * 0.4f

        particles.forEach { particle ->
            drawParticle(particle, center, maxRadius, time, colors)
        }
    }
}

/**
 * Parámetros de una partícula individual.
 *
 * @property orbitRadiusX Radio de la órbita en X (como fracción del maxRadius).
 * @property orbitRadiusY Radio de la órbita en Y (como fracción del maxRadius).
 * @property speed Velocidad angular de la partícula (grados/segundo).
 * @property phase Fase inicial (offset angular en la órbita).
 * @property size Tamaño del círculo (radio en dp).
 * @property alpha Opacidad de la partícula.
 * @property colorIndex Índice de color: 0=EnergyLow, 1=EnergyMedium, 2=EnergyHigh.
 */
private data class Particle(
    val orbitRadiusX: Float,
    val orbitRadiusY: Float,
    val speed: Float,
    val phase: Float,
    val size: Float,
    val alpha: Float,
    val colorIndex: Int
)

/**
 * Genera y recuerda el array de partículas con parámetros semi-aleatorios.
 *
 * POR QUÉ remember vs regenerar cada recomposición: Las partículas deben
 * mantener sus órbitas consistentes entre frames. Si regeneráramos los
 * parámetros aleatorios en cada recomposición, las partículas saltarían.
 * remember asegura que los parámetros se calculen una sola vez.
 */
@Composable
private fun rememberParticles(): List<Particle> {
    val particleCount = 30.coerceAtMost(MAX_PARTICLES)

    return androidx.compose.runtime.remember {
        List(particleCount) {
            Particle(
                orbitRadiusX = Random.nextFloat() * 0.5f + 0.3f,
                orbitRadiusY = Random.nextFloat() * 0.5f + 0.3f,
                speed = Random.nextFloat() * 0.3f + 0.1f,
                phase = Random.nextFloat() * 360f,
                size = Random.nextFloat() * 3f + 1.5f,
                alpha = Random.nextFloat() * 0.4f + 0.1f,
                colorIndex = Random.nextInt(3)
            )
        }
    }
}

/**
 * Dibuja una partícula en su posición orbital calculada.
 *
 * La posición se calcula como:
 *   x = center.x + cos(angle) * orbitRadius * maxRadius
 *   y = center.y + sin(angle) * orbitRadius * maxRadius
 *
 * El ángulo = time * speed + phase (movimiento continuo).
 */
private fun DrawScope.drawParticle(
    particle: Particle,
    center: Offset,
    maxRadius: Float,
    time: Float,
    colors: AndroidDropColorScheme
) {
    val angle = Math.toRadians((time * particle.speed + particle.phase).toDouble())

    val x = center.x + (cos(angle) * particle.orbitRadiusX * maxRadius).toFloat()
    val y = center.y + (sin(angle) * particle.orbitRadiusY * maxRadius).toFloat()

    val color = when (particle.colorIndex) {
        0 -> colors.energyLow
        1 -> colors.energyMedium
        else -> colors.energyHigh
    }

    drawCircle(
        color = color.copy(alpha = particle.alpha),
        radius = particle.size * density,
        center = Offset(x, y)
    )
}
