package com.androiddrop.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.androiddrop.core.ui.theme.LocalAndroidDropColorScheme

/**
 * Anillo de progreso animado con cambio de color dinámico.
 *
 * POR QUÉ un anillo vs barra de progreso: El anillo circular es más compacto
 * y puede superponerse a otros elementos (como el icono de archivo). Además,
 * el cambio de color según el progreso (EnergyLow → EnergyMedium → EnergyHigh)
 * proporciona información adicional: el usuario puede saber el estado de la
 * transferencia de un vistazo sin leer el porcentaje.
 *
 * Escala de colores según progreso:
 *   0-33%  → EnergyLow  (verde)
 *   33-66% → EnergyMedium (verde neón)
 *   66-100% → EnergyHigh (cian)
 *
 * @param progress Progreso actual (0.0 a 1.0).
 * @param modifier Modificador para tamaño y posicionamiento.
 * @param strokeWidth Grosor del anillo en dp.
 * @param color Color personalizado (si es null, se calcula según progreso).
 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 4f,
    color: Color? = null
) {
    val colors = LocalAndroidDropColorScheme.current

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )

    val ringColor = color ?: when {
        animatedProgress < 0.33f -> colors.energyLow
        animatedProgress < 0.66f -> colors.energyMedium
        else -> colors.energyHigh
    }

    Canvas(modifier = modifier) {
        val canvasSize = size.width.coerceAtMost(size.height)
        val stroke = strokeWidth * density
        val arcSize = Size(canvasSize - stroke, canvasSize - stroke)
        val topLeft = Offset(stroke / 2, stroke / 2)

        // Fondo del anillo (trazo completo, semi-transparente)
        drawArc(
            color = Color.White.copy(alpha = 0.1f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )

        // Progreso activo (trazo parcial)
        drawArc(
            color = ringColor,
            startAngle = -90f,
            sweepAngle = animatedProgress * 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}
