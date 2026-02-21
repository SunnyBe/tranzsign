package com.sunday.tranzsign.domain.entity

// Enum to define the different ways a user can sign a transaction.
enum class SigningStrategy {
    PASSKEY,
    OTP,
    BIOMETRIC
}