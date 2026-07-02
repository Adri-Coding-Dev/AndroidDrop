package com.androiddrop.domain.repository

import com.androiddrop.domain.model.FileNode
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    fun getFiles(directoryUri: String): Flow<List<FileNode>>
    fun getFileTree(rootUri: String): Flow<FileNode>
    suspend fun getFileInfo(uri: String): FileNode
    suspend fun getThumbnail(uri: String, maxSize: Int): ByteArray?
    suspend fun exists(uri: String): Boolean
}
