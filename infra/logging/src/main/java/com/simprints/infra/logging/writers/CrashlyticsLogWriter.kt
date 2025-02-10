package com.simprints.infra.logging.writers

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Message
import co.touchlab.kermit.MessageStringFormatter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Tag
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.simprints.infra.logging.Simber.USER_PROPERTY_TAG

internal class CrashlyticsLogWriter(
    private val crashlytics: FirebaseCrashlytics,
    private val messageStringFormatter: MessageStringFormatter = OptionalTagFormatter(),
) : LogWriter() {
    override fun isLoggable(
        tag: String,
        severity: Severity,
    ): Boolean = severity >= Severity.Info

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?,
    ) {
        if (tag.contains(USER_PROPERTY_TAG)) {
            val originalTag = tag.removePrefix(USER_PROPERTY_TAG)
            crashlytics.setCustomKey(originalTag, message)
            return
        }

        val message = messageStringFormatter.formatMessage(severity, Tag(tag), Message(message))

        if (severity == Severity.Info) {
            crashlytics.log(message)
        }

        if (severity == Severity.Warn || severity == Severity.Error) {
            if (throwable != null) {
                crashlytics.log(message)
                crashlytics.recordException(throwable)
            } else {
                crashlytics.recordException(Exception(message))
            }
        }
    }
}
