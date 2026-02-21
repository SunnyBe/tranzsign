package com.sunday.tranzsign.data.service

import com.sunday.tranzsign.domain.service.MoneyFormatter
import com.sunday.tranzsign.domain.service.PrecisionMode
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoneyFormatterImpl @Inject constructor() : MoneyFormatter {

    override fun format(
        amount: BigDecimal,
        currencyCode: String,
        precisionMode: PrecisionMode
    ): String {
        val locale = Locale.getDefault()
        val numberFormat = NumberFormat.getNumberInstance(locale)

        val isTinyButNonZero = amount > BigDecimal.ZERO &&
                amount.movePointRight(precisionMode.max) < BigDecimal.ONE

        if (isTinyButNonZero) {
            // Show a user-friendly message for very small amounts that would otherwise display as "0.00000000"
            return "< ${1.toBigDecimal().movePointLeft(precisionMode.max).toPlainString()} ETH"
        }

        numberFormat.minimumFractionDigits = precisionMode.min
        numberFormat.maximumFractionDigits = precisionMode.max
        numberFormat.roundingMode = RoundingMode.DOWN

        val formattedNumber = numberFormat.format(amount)

        return if (isSymbolAtEnd(locale)) { // Better UX for right-to-left languages
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
}