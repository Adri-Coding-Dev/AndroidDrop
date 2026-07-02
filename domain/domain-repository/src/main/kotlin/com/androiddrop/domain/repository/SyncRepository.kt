package com.androiddrop.domain.repository

import com.androiddrop.domain.model.SyncFrame
import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    fun sendSyncFrame(frame: SyncFrame): Flow<Result<Unit>>
    fun receiveSyncFrames(): Flow<SyncFrame>
    suspend fun calculateClockDrift(masterTimestamp: Long): Long
}
