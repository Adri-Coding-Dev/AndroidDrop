package com.androiddrop.domain.usecase

import com.androiddrop.core.common.DefaultRepo
import com.androiddrop.domain.repository.TransferRepository
import javax.inject.Inject

class CancelTransferUseCase @Inject constructor(
    @DefaultRepo private val transferRepository: TransferRepository
) {
    suspend operator fun invoke(sessionId: String) {
        transferRepository.cancelTransfer(sessionId)
    }
}
