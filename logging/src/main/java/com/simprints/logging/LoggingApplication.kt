package com.simprints.logging

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.simprints.core.CoreApplication
import timber.log.Timber

class LoggingApplication: CoreApplication() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree(FirebaseCrashlytics.getInstance()))
            Timber.plant(AnalyticsTree(FirebaseAnalytics.getInstance(this)))
        }

    }

}


