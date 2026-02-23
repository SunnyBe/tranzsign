package com.sunday.tranzsign

import com.sunday.tranzsign.domain.entity.TimeRemaining
import com.sunday.tranzsign.domain.entity.TimeRemainingUnit
import com.sunday.tranzsign.domain.util.TimeUtil
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeUtilTest {

    @Test
    fun `calculateTimeRemaining returns Expired when expiry is in the past`() {
        val now = 1000L
        val expiry = 500L

        val result = TimeUtil.calculateTimeRemaining(expiry, now)

        assertEquals(TimeRemaining.Expired, result)
    }

    @Test
    fun `calculateTimeRemaining returns Seconds when diff is less than a minute`() {
        val now = 0L
        val expiry = 45_000L // 45 seconds

        val result = TimeUtil.calculateTimeRemaining(expiry, now)

        val expected = TimeRemaining.Active(45, TimeRemainingUnit.SECONDS)
        assertEquals(expected, result)
    }

    @Test
    fun `calculateTimeRemaining returns Minutes when diff is exactly 5 minutes`() {
        val now = 10_000L
        val expiry = 10_000L + (5 * 60 * 1000)

        val result = TimeUtil.calculateTimeRemaining(expiry, now)

        val expected = TimeRemaining.Active(5, TimeRemainingUnit.MINUTES)
        assertEquals(expected, result)
    }

    @Test
    fun `calculateTimeRemaining returns Hours when diff is 2 hours`() {
        val now = 0L
        val expiry = 2 * 60 * 60 * 1000L

        val result = TimeUtil.calculateTimeRemaining(expiry, now)

        val expected = TimeRemaining.Active(2, TimeRemainingUnit.HOURS)
        assertEquals(expected, result)
    }
}