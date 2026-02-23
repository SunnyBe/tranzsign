package com.sunday.tranzsign.ui.feature.signtransaction

import com.sunday.tranzsign.R

// Enum representing different transaction authentication strategies.
enum class TransactionAuthStrategy(val displayName: String, val icon: Int) {
    PASSKEY("Passkey", R.drawable.ic_key_24),
    OTP("One-Time Password", R.drawable.outline_pin_24),
    BIOMETRIC("Biometric", R.drawable.ic_fingerprint_24)
}