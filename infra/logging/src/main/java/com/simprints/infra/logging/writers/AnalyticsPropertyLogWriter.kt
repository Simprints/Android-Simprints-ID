package com.simprints.infra.logging.writers

import androidx.core.os.bundleOf
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.LoggingConstants.firebaseLoggableTags
import com.simprints.infra.logging.Simber

internal class AnalyticsPropertyLogWriter(
    private val analytics: FirebaseAnalytics,
) : LogWriter() {
    override fun isLoggable(
        tag: String,
        severity: Severity,
    ) = severity == Severity.Info && (firebaseLoggableTags.contains(tag) || tag.contains(Simber.USER_PROPERTY_TAG))

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?,
    ) {
        if (firebaseLoggableTags.contains(tag)) {
            val params = bundleOf(MESSAGE_PARAM to message)
            // More info about the throwable will be logged as an exception in crashlytics
            throwable?.let {
                params.putString(EXCEPTION_TYPE_PARAM, it::class.java.simpleName)
                params.putString(EXCEPTION_MESSAGE_PARAM, it.message)
            }
            analytics.logEvent(tag, params)
        }
        val originalTag = tag.removePrefix(Simber.USER_PROPERTY_TAG)

        if (originalTag == LoggingConstants.AnalyticsUserProperties.USER_ID) {
            analytics.setUserId(message)
        } else {
            analytics.setUserProperty(originalTag, message)
        }
    }

    companion object {
        const val MESSAGE_PARAM = "message"
        const val EXCEPTION_TYPE_PARAM = "exception_type"
        const val EXCEPTION_MESSAGE_PARAM = "exception_message"
    }
}
