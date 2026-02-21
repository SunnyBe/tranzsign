package com.sunday.tranzsign.ui.feature.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sunday.tranzsign.ui.navigation.AppNavigator
import com.sunday.tranzsign.ui.theme.TranzSignTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.tag(LOG_TAG).d("onCreate called")
        enableEdgeToEdge()
        setContent {
            TranzSignTheme {
                AppNavigator()
            }
        }
    }

    companion object {
        const val LOG_TAG = "MainActivity"
    }
}