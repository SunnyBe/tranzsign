package com.sunday.tranzsign.domain.usecase.signtransaction

/**
 * Represents the state of the signing process.
 * Success here means transaction was signed and submitted successfully, while Error indicates a failure at any step.
 */
sealed interface SigningState {
    data object Idle : SigningState
    data object InProgress : SigningState
    data class Success(val message: String) : SigningState
    data class Error(val message: String) : SigningState
}