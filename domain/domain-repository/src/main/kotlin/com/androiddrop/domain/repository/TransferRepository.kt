package com.androiddrop.domain.repository

import com.androiddrop.domain.model.FileNode
import com.androiddrop.domain.model.NearbyDevice
import com.androiddrop.domain.model.TransferProgress
import com.androiddrop.domain.model.TransferSession
import kotlinx.coroutines.flow.Flow

interface TransferRepository {
    suspend fun createSession(file: FileNode, targetDevice: NearbyDevice): TransferSession
    fun getSession(sessionId: String): Flow<TransferSession?>
    fun transferFile(session: TransferSession): Flow<TransferProgress>
    suspend fun cancelTransfer(sessionId: String)
    suspend fun verifyTransfer(sessionId: String): Boolean
    fun getActiveTransfers(): Flow<List<TransferSession>>
}
