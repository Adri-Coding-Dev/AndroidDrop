package com.androiddrop.data.transfer

import kotlinx.serialization.Serializable

@Serializable
data class FileMetadata(
    val name: String,
    val size: Long,
    val mimeType: String,
    val checksum: ByteArray,
    val chunkSize: Int,
    val totalChunks: Int
)
