package com.sunday.tranzsign.ui.feature.withdrawal

import com.sunday.tranzsign.domain.entity.OperationType
import com.sunday.tranzsign.domain.entity.TransactionQuotation
import com.sunday.tranzsign.domain.usecase.signtransaction.SigningRequest
import com.sunday.tranzsign.ui.feature.signtransaction.TransactionAuthStrategy

sealed interface WithdrawalIntent {
    data class AmountChanged(val amount: String) : WithdrawalIntent
    data class DecideQuotation(
        val quotation: TransactionQuotation,
        val operationType: OperationType,
        val isExpired: Boolean
    ) : WithdrawalIntent

    data class RequestQuotation(val operationType: OperationType) : WithdrawalIntent
    data class ConfirmQuotation(
        val quotation: TransactionQuotation,
        val operationType: OperationType
    ) : WithdrawalIntent

    data class SignTransaction(
        val signingRequest: SigningRequest,
        val authStrategy: TransactionAuthStrategy
    ) : WithdrawalIntent

    data object CompleteTransaction : WithdrawalIntent
    data object DismissDialog : WithdrawalIntent
}