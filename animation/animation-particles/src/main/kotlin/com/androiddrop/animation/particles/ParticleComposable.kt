package com.androiddrop.animation.particles

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas

/**
 * Vista Composable que renderiza el sistema de partículas usando Canvas.
 *
 * POR QUÉ Canvas de Compose vs AndroidView: El sistema de partículas renderiza
 * usando el Canvas de Android (no OpenGL), por lo que se integra directamente
 * con el Canvas de Compose. Esto evita la sobrecarga de AndroidView y permite
 * que las partículas se rendericen como parte del árbol de composición,
 * heredando recortes, transforms y alpha del padre.
 *
 * @param system   Sistema de partículas a renderizar.
 * @param modifier Modifier de Compose para tamaño, padding, etc.
 */
@Composable
fun ParticleView(
    system: ParticleSystem,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        system.render(drawContext.canvas.nativeCanvas)
    }
}
