package com.androiddrop.domain.model

import com.androiddrop.core.common.Vector3

sealed interface SphereState {
    data object Idle : SphereState
    data object Found : SphereState
    data class Held(val position: Vector3, val velocity: Vector3) : SphereState
    data class Launched(val velocity: Vector3, val targetPosition: Vector3) : SphereState
    data object Entering : SphereState
    data class Decaying(val progress: Float) : SphereState
    data object Exploding : SphereState
}
