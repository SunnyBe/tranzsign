package com.sunday.tranzsign.domain.util

import com.sunday.tranzsign.domain.entity.TimeRemaining
import com.sunday.tranzsign.domain.entity.TimeRemainingUnit

object TimeUtil {
    fun calculateTimeRemaining(
        expiryTimestamp: Long,
        now: Long = System.currentTimeMillis()
    ): TimeRemaining {
        val diff = expiryTimestamp - now
        if (diff <= 0) return TimeRemaining.Expired

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> TimeRemaining.Active(days.toInt(), TimeRemainingUnit.DAYS)
            hours > 0 -> TimeRemaining.Active(hours.toInt(), TimeRemainingUnit.HOURS)
            minutes > 0 -> TimeRemaining.Active(minutes.toInt(), TimeRemainingUnit.MINUTES)
            else -> TimeRemaining.Active(seconds.toInt(), TimeRemainingUnit.SECONDS)
        }
    }
}