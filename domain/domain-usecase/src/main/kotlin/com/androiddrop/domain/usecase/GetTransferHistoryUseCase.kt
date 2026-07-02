package com.androiddrop.domain.usecase

import com.androiddrop.core.common.DefaultRepo
import com.androiddrop.domain.model.TransferSession
import com.androiddrop.domain.repository.TransferRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransferHistoryUseCase @Inject constructor(
    @DefaultRepo private val transferRepository: TransferRepository
) {
    fun getActiveTransfers(): Flow<List<TransferSession>> {
        return transferRepository.getActiveTransfers()
    }
}
