package com.sunday.tranzsign.ui.feature.withdrawal

import com.sunday.tranzsign.domain.entity.OperationType
import com.sunday.tranzsign.ui.feature.signtransaction.TransactionSigningStrategy

sealed interface WithdrawalIntent {
    data class AmountChanged(val amount: String) : WithdrawalIntent
    data class RequestQuotation(val operationType: OperationType) : WithdrawalIntent
    data object ConfirmQuotation : WithdrawalIntent
    data class SignTransaction(val strategy: TransactionSigningStrategy) : WithdrawalIntent
    data object CompleteTransaction : WithdrawalIntent
    data object DismissDialog : WithdrawalIntent
}