package com.simprints.logging

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

object SimberBuilder {

    /**
     * Simber needs to be initialized with the application context. It can be initialized multiple
     * times without issue. Re-initializing Simber uproots and replants all trees.
     * @param context Application Context
     */
    fun initialize(context: Application){
        Timber.uprootAll()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree(FirebaseCrashlytics.getInstance()))
            Timber.plant(AnalyticsTree(FirebaseAnalytics.getInstance(context)))
        }
    }

}
