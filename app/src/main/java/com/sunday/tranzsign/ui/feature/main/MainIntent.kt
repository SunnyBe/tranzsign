package com.sunday.tranzsign.ui.feature.main

sealed interface MainIntent {
    data class SelectFeature(val selectedId: Int) : MainIntent
    data object RefreshBalance : MainIntent
}