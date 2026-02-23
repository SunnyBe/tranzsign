package com.sunday.tranzsign.domain.usecase.signtransaction

import com.sunday.tranzsign.domain.entity.AuthStrategy
import com.sunday.tranzsign.domain.service.SignatureService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SignTransactionUseCase @Inject constructor(
    private val signatureService: SignatureService
) {
    operator fun invoke(
        quotationId: String,
        signingRequest: SigningRequest,
        authStrategy: AuthStrategy
    ): Flow<SignTransactionState> = flow {
        emit(SignTransactionState.InProgress)
        val signedChallenge = signatureService.sign(
            signingRequest = signingRequest,
            authStrategy = authStrategy
        )
        val result = SignTransactionResult(
            quotationId = quotationId,
            signedChallenge = signedChallenge
        )
        emit(SignTransactionState.Success(result))
    }.catch {
        emit(SignTransactionState.Error("An error occurred during signing: ${it.message}"))
    }
}

// BETTER: Move to a separate file.
sealed interface SignTransactionState {
    object InProgress : SignTransactionState
    data class Success(val result: SignTransactionResult) : SignTransactionState
    data class Error(val message: String) : SignTransactionState
}

// BETTER: Move to a separate file.
data class SignTransactionResult(
    val quotationId: String,
    val signedChallenge: String
)