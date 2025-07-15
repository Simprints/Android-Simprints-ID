package com.simprints.infra.logging

import android.content.Context
import android.os.Build
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
     * Initializes the Simber logging framework with appropriate writers and severity based on build type.
     *
     * - Debug builds use `LogcatWriter` with `Severity.Debug`.
     * - Release builds use `AnalyticsPropertyLogWriter` and `CrashlyticsLogWriter` with `Severity.Info`.
     *   - `FileLogWriter` is added for Android versions starting from API26.
     */
    fun initialize(context: Context) {
        when {
            BuildConfig.DEBUG -> Logger.setLogWriters(LogcatWriter())

            Build.VERSION.SDK_INT < Build.VERSION_CODES.O -> Logger.setLogWriters(
                AnalyticsPropertyLogWriter(FirebaseAnalytics.getInstance(context)),
                CrashlyticsLogWriter(FirebaseCrashlytics.getInstance()),
                // Skip file logger to avoid crashes on Android 6-7 - https://simprints.atlassian.net/browse/MS-1060
            )

            else -> Logger.setLogWriters(
                AnalyticsPropertyLogWriter(FirebaseAnalytics.getInstance(context)),
                CrashlyticsLogWriter(FirebaseCrashlytics.getInstance()),
                FileLogWriter(context),
            )
        }

        Logger.setMinSeverity(if (BuildConfig.DEBUG) Severity.Debug else Severity.Info)
    }
}
