package com.sunday.tranzsign

import com.sunday.tranzsign.data.service.MoneyFormatterImpl
import com.sunday.tranzsign.domain.service.PrecisionMode
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.math.BigInteger
import java.util.Locale

class MoneyFormatterImplTest {

    private lateinit var formatter: MoneyFormatterImpl

    @Before
    fun setUp() {
        formatter = MoneyFormatterImpl()
    }

    @Test
    fun `format returns standard precision with fixed decimals for US locale`() {
        Locale.setDefault(Locale.US)
        // 1.234567 ETH -> Standard (4 decimals) -> 1.2345
        val amountInWei = BigInteger("1234567000000000000")

        val result = formatter.format(amountInWei, "ETH", PrecisionMode.Standard)

        assertEquals("ETH 1.2345", result)
    }

    @Test
    fun `format respects German locale with comma separator`() {
        Locale.setDefault(Locale.GERMANY)
        // In Germany, ETH usually follows the number: "1,2345 ETH"
        val amountInWei = BigInteger("1234567000000000000")

        val result = formatter.format(amountInWei, "ETH", PrecisionMode.Standard)

        assertEquals("ETH 1,2345", result)
    }

    @Test
    fun `format truncates rather than rounding up`() {
        Locale.setDefault(Locale.US)
        // 1.234599 ETH -> Standard (4 decimals) -> Should be 1.2345, NOT 1.2346
        val amountInWei = BigInteger("1234599000000000000")

        val result = formatter.format(amountInWei, "ETH", PrecisionMode.Standard)

        assertEquals("ETH 1.2345", result)
    }

    @Test
    fun `format handles tiny amounts below threshold`() {
        Locale.setDefault(Locale.US)
        // 0.00001 ETH -> Standard threshold is 0.0001
        val tinyAmount = BigInteger("10000000000000") // 0.00001 ETH

        val result = formatter.format(tinyAmount, "ETH", PrecisionMode.Standard)

        assertEquals("< ETH 0.0001", result)
    }

    @Test
    fun `format shows detailed precision for gas fees`() {
        Locale.setDefault(Locale.US)
        // 0.0000123456 ETH -> Detailed (8 decimals)
        val feeInWei = BigInteger("12345600000000")

        val result = formatter.format(feeInWei, "ETH", PrecisionMode.Detail)

        assertEquals("ETH 0.00001234", result)
    }

    @Test
    fun `format handles zero correctly`() {
        Locale.setDefault(Locale.US)
        val result = formatter.format(BigInteger.ZERO, "ETH", PrecisionMode.Standard)

        assertEquals("ETH 0.0000", result)
    }
}