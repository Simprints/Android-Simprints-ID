package com.simprints.infra.logging

import android.content.Context
import co.touchlab.kermit.LogcatWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.simprints.infra.logging.writers.AnalyticsPropertyLogWriter
import com.simprints.infra.logging.writers.CrashlyticsLogWriter
import com.simprints.infra.logging.writers.FileLogWriter

object SimberBuilder {
    /**
     * Simber needs to be initialized with the application context. It can be initialized multiple
     * times without issue. Re-initializing Simber uproots and replants all trees.
     * @param context Application Context
     */
    fun initialize(context: Context) {
        if (BuildConfig.DEBUG) {
            Logger.setLogWriters(LogcatWriter())
            Logger.setMinSeverity(Severity.Debug)
        } else {
            Logger.setLogWriters(
                AnalyticsPropertyLogWriter(FirebaseAnalytics.getInstance(context)),
                CrashlyticsLogWriter(FirebaseCrashlytics.getInstance()),
                FileLogWriter(context),
            )
            Logger.setMinSeverity(Severity.Info)
        }
    }
}
