package com.sunday.tranzsign.domain.entity

// Enum to define the different ways a user can authorize a transaction.
enum class AuthStrategy {
    PASSKEY,
    OTP,
    BIOMETRIC
}