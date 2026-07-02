package com.androiddrop.domain.model

sealed interface TransferProgress {
    data object Idle : TransferProgress
    data class Preparing(val percent: Float) : TransferProgress
    data class Transferring(
        val bytesTransferred: Long,
        val totalBytes: Long,
        val speedBps: Long,
        val estimatedTimeRemainingMs: Long = 0L,
        val currentChunk: Int = 0,
        val totalChunks: Int = 0
    ) : TransferProgress
    data object Verifying : TransferProgress
    data object Complete : TransferProgress
    data class Failed(val error: TransferError) : TransferProgress
}
