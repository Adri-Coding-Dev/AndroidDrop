package com.androiddrop.sync.protocol

import com.androiddrop.domain.model.SyncFrame as DomainSyncFrame
import com.androiddrop.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val syncProtocol: SyncProtocol
) : SyncRepository {
    override fun sendSyncFrame(frame: DomainSyncFrame): Flow<Result<Unit>> = flow {
        try {
            val protocolFrame = SyncFrame(
                timestamp = frame.masterTimestamp,
                position = frame.spherePosition,
                scale = frame.sphereScale,
                energy = frame.sphereEnergy,
                phase = frame.phase.ordinal
            )
            syncProtocol.sendSync(protocolFrame)
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun receiveSyncFrames(): Flow<DomainSyncFrame> = flow {
        syncProtocol.receiveSync().collect { frame ->
            emit(DomainSyncFrame(
                phase = com.androiddrop.domain.model.TransferPhase.entries.getOrElse(frame.phase) { 
                    com.androiddrop.domain.model.TransferPhase.IDLE 
                },
                progress = 0f,
                sphereEnergy = frame.energy,
                sphereScale = frame.scale,
                spherePosition = frame.position,
                portalIntensity = 0f,
                particleCount = 0,
                masterTimestamp = frame.timestamp,
                localTimestamp = System.currentTimeMillis()
            ))
        }
    }

    override suspend fun calculateClockDrift(masterTimestamp: Long): Long {
        return syncProtocol.negotiateClock()
    }
}
