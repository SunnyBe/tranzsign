package com.sunday.tranzsign.domain.entity

import java.math.BigInteger

data class TransactionQuotation(
    val id: String,
    val amountInWei: BigInteger,
    val feeInWei: BigInteger,
    val challenge: String, // The challenge to be signed
    val expiresAt: Long
)