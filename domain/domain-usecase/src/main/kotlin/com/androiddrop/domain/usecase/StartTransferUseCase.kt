package com.androiddrop.domain.usecase

import com.androiddrop.core.common.DefaultRepo
import com.androiddrop.domain.model.FileNode
import com.androiddrop.domain.model.NearbyDevice
import com.androiddrop.domain.model.TransferProgress
import com.androiddrop.domain.repository.TransferRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class StartTransferUseCase @Inject constructor(
    @DefaultRepo private val transferRepository: TransferRepository
) {
    operator fun invoke(file: FileNode, targetDevice: NearbyDevice): Flow<TransferProgress> = flow {
        val session = transferRepository.createSession(file, targetDevice)
        emitAll(transferRepository.transferFile(session))
    }
}
