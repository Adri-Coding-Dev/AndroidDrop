package com.androiddrop.animation.portal

import android.opengl.GLSurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Vista Composable que integra el renderizado OpenGL del portal.
 *
 * POR QUÉ AndroidView + GLSurfaceView vs Canvas nativo: El portal requiere
 * shaders programables y múltiples passes de renderizado (FBO), lo que solo
 * es posible con OpenGL ES 3.0. GLSurfaceView proporciona el contexto EGL
 * y el loop de renderizado, mientras que AndroidView permite incrustarlo
 * en la jerarquía de Compose.
 *
 * @param state    Estado actual del portal (fase, rotación, profundidad, etc.).
 * @param modifier Modifier de Compose para tamaño, padding, etc.
 */
@Composable
fun PortalView(
    state: PortalRenderState,
    modifier: Modifier = Modifier
) {
    val renderer = remember { PortalRenderer() }

    // Actualizar el estado del renderer cuando cambia el state
    DisposableEffect(state) {
        renderer.updateState(state)
        onDispose { }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            GLSurfaceView(context).apply {
                setEGLContextClientVersion(3)
                setRenderer(renderer)
                renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            }
        },
        update = { glSurfaceView ->
            // Forzar renderizado cuando el estado cambia
            glSurfaceView.requestRender()
        }
    )
}
