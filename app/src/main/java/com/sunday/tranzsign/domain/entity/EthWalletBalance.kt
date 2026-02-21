package com.sunday.tranzsign.domain.entity

import java.math.BigInteger

data class EthWalletBalance(
    val balanceInWei: BigInteger,
    val lastUpdatedMillis: Long
)