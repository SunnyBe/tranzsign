package com.sunday.tranzsign.domain.service

import java.math.BigInteger
import java.util.Locale

/**
 * Formats currency values into localized strings.
 * For simplicity, only supports ETH for now, but can be extended to support other currencies in the future.
 * The implementation should handle formatting according to the user's locale, including decimal separators and currency symbols.
 */
interface MoneyFormatter {
    fun format(
        amountInWei: BigInteger,
        currencyCode: String = "ETH",
        precisionMode: PrecisionMode = PrecisionMode.Detail,
        locale: Locale = Locale.getDefault()
    ): String
}

enum class PrecisionMode(val min: Int, val max: Int) {
    Standard(min = 4, max = 4), // For Balances/Inputs
    Detail(min = 8, max = 8),   // For Gas/Fees
}