package com.androiddrop.feature.transfer

import com.androiddrop.core.common.Vector3

sealed interface GestureEvent {
    data class Drag(val position: Vector3) : GestureEvent
    data class Fling(val velocity: Vector3) : GestureEvent
    data object Tap : GestureEvent
}
