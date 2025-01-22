package com.simprints.infra.logging.trees

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.simprints.infra.logging.Simber.USER_PROPERTY_TAG
import timber.log.Timber

internal class CrashReportingTree(
    private val crashlytics: FirebaseCrashlytics,
) : Timber.Tree() {
    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?,
    ) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) return

        if (tag != null && tag.contains(USER_PROPERTY_TAG)) {
            val originalTag = tag.removePrefix(USER_PROPERTY_TAG)
            crashlytics.setCustomKey(originalTag, message)
            return
        }

        if (priority == Log.INFO) {
            logMessageWithTag(tag, message)
        }

        if (priority == Log.WARN || priority == Log.ERROR) {
            if (t != null) {
                logMessageWithTag(tag, message)
                crashlytics.recordException(t)
            } else {
                crashlytics.recordException(Exception(message))
            }
        }
    }

    private fun logMessageWithTag(
        tag: String?,
        message: String,
    ) {
        // Custom crashlytics logs do not have the tag field
        if (tag.isNullOrBlank()) {
            crashlytics.log(message)
        } else {
            crashlytics.log("[$tag] $message")
        }
    }
}
