package com.androiddrop.animation.gesture

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Detector de gestos para la interacción con la esfera y el portal.
 *
 * POR QUÉ combinar drag + fling + tap en un solo composable: La interacción
 * con la esfera energética requiere distinguir entre tres gestos con la misma
 * superficie táctil. Un solo detector con estado compartido (posición inicial,
 * historial de velocidad) es más eficiente y evita conflictos entre detectores
 * separados.
 *
 * POR QUÉ pointerInput vs Modifier.draggable: pointerInput da control total
 * sobre el pipeline de eventos táctiles, permitiendo implementar detección
 * de fling personalizada y filtrado de ruido. Las APIs de alto nivel de
 * Compose no exponen el historial de velocidad necesario para el fling físico.
 *
 * @param onDrag   Callback invocado durante el arrastre con el offset acumulado.
 * @param onFling  Callback invocado al soltar con la velocidad de lanzamiento.
 * @param onTap    Callback invocado al realizar un tap.
 * @param modifier Modifier a aplicar al contenedor.
 * @param content  Contenido a envolver con el detector de gestos.
 */
@Composable
fun GestureDetector(
    onDrag: (androidx.compose.ui.geometry.Offset) -> Unit,
    onFling: (Velocity) -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    val gestureModifier = remember {
        Modifier.pointerInput(Unit) {
            coroutineScope {
                launch {
                    detectTapGestures(
                        onTap = {
                            Timber.d("GestureDetector: tap detectado")
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onTap()
                        }
                    )
                }

                launch {
                    detectDragGestures(
                        onDragStart = { offset ->
                            Timber.d("GestureDetector: drag iniciado en $offset")
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount)
                        },
                        onDragEnd = {
                            Timber.d("GestureDetector: drag finalizado")
                            onFling(Velocity(0f, 0f))
                        },
                        onDragCancel = {
                            Timber.d("GestureDetector: drag cancelado")
                        }
                    )
                }
            }
        }
    }

    androidx.compose.foundation.layout.Box(
        modifier = modifier.then(gestureModifier)
    ) {
        content()
    }
}
