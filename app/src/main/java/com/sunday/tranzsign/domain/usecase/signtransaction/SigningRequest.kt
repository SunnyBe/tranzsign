package com.sunday.tranzsign.domain.usecase.signtransaction

import com.sunday.tranzsign.domain.entity.OperationType
import com.sunday.tranzsign.domain.entity.SigningStrategy

/**
 * Represents a request to sign a transaction, containing the necessary information for the signing process.
 *
 * @property challenge A unique string that serves as a challenge for the signing process, ensuring security and preventing replay attacks.
 * @property operationType The type of operation being signed (e.g., withdrawal, transfer, swap).
 * @property strategy The strategy used for signing the transaction (e.g., passkey, OTP, biometric).
 */
data class SigningRequest(
    val challenge: String,
    val operationType: OperationType,
    val strategy: SigningStrategy
)