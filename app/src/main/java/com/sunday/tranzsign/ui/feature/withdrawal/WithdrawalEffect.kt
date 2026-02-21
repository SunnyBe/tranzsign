package com.sunday.tranzsign.ui.feature.withdrawal

sealed interface WithdrawalEffect {
    data object NavigateToMain : WithdrawalEffect
}