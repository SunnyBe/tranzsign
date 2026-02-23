package com.sunday.tranzsign.data.service

import com.sunday.tranzsign.domain.service.MoneyFormatter
import com.sunday.tranzsign.domain.service.PrecisionMode
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoneyFormatterImpl @Inject constructor() : MoneyFormatter {

    override fun format(
        amountInWei: BigInteger,
        currencyCode: String,
        precisionMode: PrecisionMode,
        locale: Locale
    ): String {
        val numberFormat = NumberFormat.getNumberInstance(locale)
        val amountInEth = amountInWei.toEth()

        val isTinyButNonZero = amountInEth > BigDecimal.ZERO &&
                amountInEth.movePointRight(precisionMode.max) < BigDecimal.ONE

        if (isTinyButNonZero) {
            val tinyAmount = 1.toBigDecimal().movePointLeft(precisionMode.max).toPlainString()

            return if (isSymbolAtEnd(locale)) {
                "< $tinyAmount $currencyCode" // Result: "< 0,0001 ETH"
            } else {
                "< $currencyCode $tinyAmount" // Result: "< ETH 0.0001"
            }
        }

        numberFormat.minimumFractionDigits = precisionMode.min
        numberFormat.maximumFractionDigits = precisionMode.max
        numberFormat.roundingMode = RoundingMode.DOWN

        val formattedNumber = numberFormat.format(amountInEth)

        // Some locales place the currency symbol after the number (e.g., "1,234.56 €")
        return if (isSymbolAtEnd(locale)) {
            "$formattedNumber $currencyCode"
        } else {
            "$currencyCode $formattedNumber"
        }
    }

    private fun isSymbolAtEnd(locale: Locale): Boolean {
        val formatter = NumberFormat.getCurrencyInstance(locale)
        return formatter.format(0).last().isLetter() ||
                formatter.format(0).startsWith('0').not() &&
                formatter.format(0).last().isDigit().not()
    }

    private fun BigInteger.toEth(): BigDecimal = this.toBigDecimal().movePointLeft(ETH_DECIMALS)

    companion object {
        private const val ETH_DECIMALS = 18
    }
}