package com.sunday.tranzsign.ui.feature.withdrawal

import androidx.annotation.StringRes
import com.sunday.tranzsign.domain.entity.TransactionQuotation

// BETTER: subdivide further into smaller state, lint will complain.
data class WithdrawalUiState(
    val quotation: TransactionQuotation? = null,
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
        val quotation: TransactionQuotation
    ) : ScreenContent

    data class ShowSignDialog(val quotation: TransactionQuotation) : ScreenContent

    data object SigningInProgress : ScreenContent
    data class ShowSuccessDialog(val message: String) : ScreenContent
    data class ShowErrorDialog(
        @param:StringRes val messageRes: Int,
        val isCritical: Boolean = false
    ) : ScreenContent
}