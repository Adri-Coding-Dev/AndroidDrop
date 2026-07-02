package com.androiddrop.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.androiddrop.core.ui.theme.LocalAndroidDropColorScheme

/**
 * Tarjeta con efecto glass morphism (SDD-07-Sistema-Diseno.md).
 *
 * POR QUÉ glass morphism: Las superficies semi-transparentes con blur crean
 * una sensación de profundidad y tecnología futurista. Las tarjetas parecen
 * "flotar" sobre el fondo, reforzando la metáfora de "energía contenida"
 * donde los datos y la UI existen en un plano etéreo sobre el hardware.
 *
 * El efecto se logra combinando:
 *   1. Fondo semi-transparente ([AndroidDropColorScheme.glassLight/Medium/Heavy])
 *   2. Borde sutil con el color primario (efecto "neón glow" contenido)
 *   3. Bordes redondeados (16dp) para suavidad visual
 *
 * @param modifier Modificador para posicionamiento y tamaño.
 * @param alpha Nivel de opacidad del glass: 0.0-1.0 (por defecto 0.3 = Medium).
 * @param content Contenido dentro de la tarjeta.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    alpha: Float = 0.3f,
    content: @Composable () -> Unit
) {
    val colors = LocalAndroidDropColorScheme.current

    val glassColor = when {
        alpha <= 0.2f -> colors.glassLight
        alpha <= 0.4f -> colors.glassMedium
        else -> colors.glassHeavy
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = glassColor),
        border = BorderStroke(
            width = 0.5.dp,
            color = colors.primary.copy(alpha = 0.3f)
        )
    ) {
        content()
    }
}
