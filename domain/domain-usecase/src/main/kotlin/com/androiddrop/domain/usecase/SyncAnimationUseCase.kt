package com.androiddrop.domain.usecase

import com.androiddrop.core.common.Vector3
import com.androiddrop.domain.model.SphereState
import com.androiddrop.domain.model.SyncFrame
import com.androiddrop.domain.model.TransferPhase
import com.androiddrop.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SyncAnimationUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {
    fun sendState(state: SphereState, progress: Float): Flow<Result<Unit>> {
        val frame = SyncFrame(
            phase = state.toTransferPhase(),
            progress = progress,
            sphereEnergy = 0f,
            sphereScale = 1f,
            spherePosition = when (state) {
                is SphereState.Held -> state.position
                is SphereState.Launched -> state.targetPosition
                else -> Vector3.zero
            },
            portalIntensity = 0f,
            particleCount = 0,
            masterTimestamp = System.currentTimeMillis(),
            localTimestamp = System.currentTimeMillis()
        )
        return syncRepository.sendSyncFrame(frame)
    }

    fun receiveSyncFrames(): Flow<SyncFrame> {
        return syncRepository.receiveSyncFrames()
    }
}

private fun SphereState.toTransferPhase(): TransferPhase = when (this) {
    SphereState.Idle -> TransferPhase.IDLE
    SphereState.Found -> TransferPhase.DEVICE_FOUND
    is SphereState.Held -> TransferPhase.SPHERE_HELD
    is SphereState.Launched -> TransferPhase.SPHERE_LAUNCHED
    SphereState.Entering -> TransferPhase.ENTERING_PORTAL
    is SphereState.Decaying -> TransferPhase.SPHERE_DECAYING
    SphereState.Exploding -> TransferPhase.SPHERE_EXPLODING
}
