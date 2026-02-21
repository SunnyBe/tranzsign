package com.sunday.tranzsign.domain.usecase.signtransaction

import com.sunday.tranzsign.domain.entity.OperationType
import com.sunday.tranzsign.domain.entity.SigningStrategy
import com.sunday.tranzsign.domain.entity.TransactionQuotation
import com.sunday.tranzsign.domain.service.TransactionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Orchestrates the end-to-end signing and submission workflow.
 * * DESIGN NOTE: This UseCase is stateful to ensure atomicity between
 * cryptographic signing and network submission. This prevents "orphaned"
 * signatures and provides a single source of truth for the transaction
 * lifecycle state (InProgress, Success, Error).
 */
class SignTransactionUseCaseImpl @Inject constructor(
    private val transactionService: TransactionService
) : SignTransactionUseCase {

    private val _state = MutableStateFlow<SigningState>(SigningState.Idle)
    override val state = _state.asStateFlow()

    override suspend fun execute(
        quotation: TransactionQuotation,
        strategy: SigningStrategy,
        operationType: OperationType
    ) {
        _state.update { SigningState.InProgress }

        try {
            val request = SigningRequest(
                challenge = quotation.challenge,
                strategy = strategy,
                operationType = operationType
            )

            val isSuccess = transactionService.submit(
                quotation = quotation,
                strategy = request.strategy
            )

            if (isSuccess) {
                _state.update { SigningState.Success("Transaction signed and submitted successfully.") }
            } else {
                _state.update { SigningState.Error("The transaction could not be signed.") }
            }
        } catch (cause: Exception) {
            _state.update { SigningState.Error("An error occurred during signing: ${cause.message}") }
        }
    }

    override fun reset() {
        _state.update { SigningState.Idle }
    }
}