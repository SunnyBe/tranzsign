package com.sunday.tranzsign.ui.feature.main

sealed interface MainEffect {
    data class ShowAlert(val alertRes: Int, val critical: Boolean = false) : MainEffect
    data object NavigateToWithdrawal : MainEffect
}