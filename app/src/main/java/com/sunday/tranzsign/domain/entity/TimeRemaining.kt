package com.sunday.tranzsign.domain.entity

sealed class TimeRemaining {
    object Expired : TimeRemaining()
    data class Active(val value: Int, val unit: TimeRemainingUnit) : TimeRemaining()
}

enum class TimeRemainingUnit { SECONDS, MINUTES, HOURS, DAYS }