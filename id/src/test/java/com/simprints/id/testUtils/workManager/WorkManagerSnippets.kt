package com.simprints.id.testUtils.workManager

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import timber.log.Timber

fun initWorkManagerIfRequired(app: Application){
    try {
        WorkManager.initialize(app, Configuration.Builder().build())
    } catch (e: IllegalStateException) {
        Timber.d("WorkManager already initialized")
    }
}
