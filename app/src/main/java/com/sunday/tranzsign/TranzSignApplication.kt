package com.sunday.tranzsign

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class TranzSignApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree()) // BETTER: plant different for prod and dev
        Timber.tag(LOG_TAG).d("setup done")
    }

    companion object {
        const val LOG_TAG = "TranzSignApplication"
    }
}