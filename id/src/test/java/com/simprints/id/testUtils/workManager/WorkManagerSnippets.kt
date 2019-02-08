package com.simprints.id.testUtils.workManager

import android.app.Application
import androidx.work.Configuration
import androidx.work.testing.WorkManagerTestInitHelper
import timber.log.Timber

fun initWorkManagerIfRequired(app: Application){
    try {
        WorkManagerTestInitHelper.initializeTestWorkManager(app, Configuration.Builder().build())
    } catch (e: IllegalStateException) {
        Timber.d("WorkManager already initialized")
    }
}
