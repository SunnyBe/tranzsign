package com.sunday.tranzsign.data.service

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.text.format.DateFormat
import com.sunday.tranzsign.domain.service.DateTimeFormatter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DateTimeFormatterImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : DateTimeFormatter {
    override fun formatFullDateTime(timestamp: Long): String {
        val skeleton = if (DateFormat.is24HourFormat(context)) "yMMMdHm" else "yMMMdjm"
        val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton)

        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()

        return formatter.format(Date(timestamp))
    }
}