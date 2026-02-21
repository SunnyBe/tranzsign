package com.sunday.tranzsign.domain.usecase.signtransaction

import com.sunday.tranzsign.domain.entity.OperationType
import com.sunday.tranzsign.domain.entity.SigningStrategy
import com.sunday.tranzsign.domain.entity.TransactionQuotation
import kotlinx.coroutines.flow.Flow

/**
 * Encapsulates the business logic for signing and submitting a transaction.
 * This makes the signing state machine reusable across different features.
 *
 * DESIGN NOTE: This UseCase is stateful to ensure atomicity between
 * cryptographic signing and network submission. This prevents "orphaned"
 * signatures and provides a single source of truth for the transaction
 * lifecycle state (InProgress, Success, Error).
 */
interface SignTransactionUseCase {
    val state: Flow<SigningState>
    suspend fun execute(
        quotation: TransactionQuotation,
        strategy: SigningStrategy,
        operationType: OperationType
    )

    fun reset()
}