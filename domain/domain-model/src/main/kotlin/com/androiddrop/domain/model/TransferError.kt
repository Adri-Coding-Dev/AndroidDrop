package com.androiddrop.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TransferError(
    val code: ErrorCode,
    val message: String,
    val recoverable: Boolean
)

@Serializable
enum class ErrorCode {
    FILE_NOT_FOUND,
    DEVICE_LOST,
    AUTH_FAILED,
    ENCRYPTION_ERROR,
    NETWORK_ERROR,
    STORAGE_FULL,
    TIMEOUT,
    UNKNOWN
}
