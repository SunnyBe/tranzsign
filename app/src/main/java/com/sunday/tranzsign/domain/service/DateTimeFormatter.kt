package com.sunday.tranzsign.domain.service

import com.sunday.tranzsign.domain.entity.TimeRemaining

/**
 * Interface for formatting date and time values in a consistent way across the app.
 */
interface DateTimeFormatter {
    fun formatFullDateTime(timestamp: Long): String
    fun formatRemainingTime(time: TimeRemaining): String
}
