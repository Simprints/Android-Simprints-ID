package com.simprints.infra.logging.writers

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber

internal class AnalyticsPropertyLogWriter(
    private val analytics: FirebaseAnalytics,
) : LogWriter() {
    override fun isLoggable(
        tag: String,
        severity: Severity,
    ): Boolean = severity == Severity.Info && tag.contains(Simber.USER_PROPERTY_TAG)

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?,
    ) {
        val originalTag = tag.removePrefix(Simber.USER_PROPERTY_TAG)

        if (originalTag == LoggingConstants.AnalyticsUserProperties.USER_ID) {
            analytics.setUserId(message)
        } else {
            analytics.setUserProperty(originalTag, message)
        }
    }
}
