package com.sunday.tranzsign.ui.feature.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

data class FeatureUiState(
    val id: Int,
    @param:StringRes val title: Int,
    @param:DrawableRes val iconRes: Int,
    val isDisabled: Boolean = false,
    val isActive: Boolean = true
)

data class WalletBalanceUiState(
    val introTitle: String = "--",
    val currentBalance: String = "--",
    val lastBalanceUpdate: String = "--"
)

@Immutable
data class MainUiState(
    val featureList: List<FeatureUiState> = emptyList(),
    val walletBalanceUiState: WalletBalanceUiState = WalletBalanceUiState()
)
