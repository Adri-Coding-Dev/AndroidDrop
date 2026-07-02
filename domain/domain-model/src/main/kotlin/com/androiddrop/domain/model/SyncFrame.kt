package com.androiddrop.domain.model

import com.androiddrop.core.common.Vector3
import kotlinx.serialization.Serializable

@Serializable
data class SyncFrame(
    val phase: TransferPhase,
    val progress: Float,
    val sphereEnergy: Float,
    val sphereScale: Float,
    val spherePosition: Vector3,
    val portalIntensity: Float,
    val particleCount: Int,
    val masterTimestamp: Long,
    val localTimestamp: Long
)

@Serializable
enum class TransferPhase {
    IDLE,
    FILE_SELECTED,
    SPHERE_TRANSFORM,
    DISCOVERING,
    DEVICE_FOUND,
    SPHERE_HELD,
    SPHERE_LAUNCHED,
    ENTERING_PORTAL,
    TRANSFERRING,
    SPHERE_DECAYING,
    SPHERE_EXPLODING,
    TRANSFER_COMPLETE,
    ERROR
}
