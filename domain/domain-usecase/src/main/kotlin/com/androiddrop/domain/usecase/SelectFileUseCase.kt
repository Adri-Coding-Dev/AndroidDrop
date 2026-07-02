package com.androiddrop.domain.usecase

import com.androiddrop.domain.model.FileNode
import com.androiddrop.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SelectFileUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    fun getFiles(directoryUri: String): Flow<List<FileNode>> {
        return fileRepository.getFiles(directoryUri)
    }

    suspend fun getDetailedFileInfo(uri: String): FileNode {
        return fileRepository.getFileInfo(uri)
    }
}
