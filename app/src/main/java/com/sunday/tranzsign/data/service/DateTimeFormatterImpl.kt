package com.sunday.tranzsign.data.service

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.text.format.DateFormat
import com.sunday.tranzsign.R
import com.sunday.tranzsign.domain.entity.TimeRemaining
import com.sunday.tranzsign.domain.entity.TimeRemainingUnit
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
        val formatter = SimpleDateFormat(pattern, Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        return formatter.format(Date(timestamp))
    }

    override fun formatRemainingTime(time: TimeRemaining): String {
        return when (time) {
            is TimeRemaining.Expired -> context.getString(R.string.expired)
            is TimeRemaining.Active -> {
                val resId = when (time.unit) {
                    TimeRemainingUnit.SECONDS -> R.plurals.remaining_time_seconds
                    TimeRemainingUnit.MINUTES -> R.plurals.remaining_time_minutes
                    TimeRemainingUnit.HOURS -> R.plurals.remaining_time_hours
                    TimeRemainingUnit.DAYS -> R.plurals.remaining_time_days
                }
                context.resources.getQuantityString(resId, time.value, time.value)
            }
        }
    }
}