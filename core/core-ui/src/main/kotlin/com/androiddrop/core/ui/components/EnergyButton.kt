package com.androiddrop.core.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.androiddrop.core.ui.theme.LocalAndroidDropColorScheme

/**
 * Botón principal con gradiente energético y efecto pulse.
 *
 * POR QUÉ este diseño: El botón representa el punto de acción principal en
 * AndroidDrop (ej: "Enviar archivo", "Aceptar transferencia"). El gradiente
 * Primary → Secondary simboliza el flujo de energía. El efecto pulse en idle
 * invita visualmente a la interacción (affordance dinámica).
 *
 * POR QUÉ isLoading: Las operaciones de red en AndroidDrop pueden tomar
 * segundos (handshake, negociación de claves). El estado de carga proporciona
 * retroalimentación inmediata al usuario de que la acción está en progreso.
 *
 * @param onClick Callback cuando el usuario presiona el botón.
 * @param text Texto del botón (semanticDescription para accesibilidad).
 * @param modifier Modificador para personalización externa.
 * @param enabled Si el botón está habilitado para interacción.
 * @param isLoading Muestra indicador de carga y deshabilita interacción.
 */
@Composable
fun EnergyButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val colors = LocalAndroidDropColorScheme.current

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Button(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription = text
        },
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = colors.glassHeavy
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = ButtonDefaults.MinWidth, height = ButtonDefaults.MinHeight),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = colors.surfaceDark,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = text,
                    color = colors.surfaceDark
                )
            }
        }
    }
}
