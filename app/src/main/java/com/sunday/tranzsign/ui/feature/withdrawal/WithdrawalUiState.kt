package com.sunday.tranzsign.ui.feature.withdrawal

import androidx.annotation.StringRes
import com.sunday.tranzsign.domain.entity.OperationType
import com.sunday.tranzsign.domain.entity.TransactionQuotation
import com.sunday.tranzsign.domain.usecase.signtransaction.SigningRequest

// BETTER: subdivide further into smaller state, lint will complain.
data class WithdrawalUiState(
    val availableBalanceFormatted: String = "",
    val remainingBalanceFormatted: String = "",
    val quotationAmountFormatted: String = "",
    val quotationFeeFormatted: String = "",
    val amountToTransferFormatted: String = "",
    val amountInput: String = "0.0",
    val ethMaxLimitFormatted: String = "0.0",
    val isCtaEnabled: Boolean = false,
    val isInsufficientBalance: Boolean = false,
    val amountExceedsLimit: Boolean = false,
    val screenContent: ScreenContent = ScreenContent.Idle
)

sealed interface ScreenContent {
    data object Idle : ScreenContent
    data object FetchingQuotation : ScreenContent
    data class ShowQuotation(
        val quotation: TransactionQuotation,
        val operationType: OperationType
    ) : ScreenContent

    data class ShowSignDialog(val signingRequest: SigningRequest) : ScreenContent

    data object SigningInProgress : ScreenContent
    data class ShowSuccessDialog(@param:StringRes val messageRes: Int) : ScreenContent
    data class ShowErrorDialog(
        @param:StringRes val messageRes: Int,
        val isCritical: Boolean = false
    ) : ScreenContent

    data class SendingTransaction(
        val quotationId: String,
        val signedChallenge: String
    ) : ScreenContent
}