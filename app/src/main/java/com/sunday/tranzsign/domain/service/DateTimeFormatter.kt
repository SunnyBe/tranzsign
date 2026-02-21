package com.sunday.tranzsign.domain.service

/**
 * Interface for formatting date and time values in a consistent way across the app.
 */
interface DateTimeFormatter {
    fun formatFullDateTime(timestamp: Long): String
}