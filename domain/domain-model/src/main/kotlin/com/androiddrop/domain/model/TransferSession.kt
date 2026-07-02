package com.androiddrop.domain.model

data class TransferSession(
    val sessionId: String,
    val file: FileNode,
    val senderDeviceId: String,
    val receiverDeviceId: String,
    val status: TransferStatus,
    val createdAt: Long,
    val updatedAt: Long
)

enum class TransferStatus {
    PENDING,
    NEGOTIATING,
    TRANSFERRING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}
