package com.simprints.logging

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.simprints.logging.Simber.USER_PROPERTY_TAG
import timber.log.Timber

internal class CrashReportingTree(private val crashlytics: FirebaseCrashlytics) : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String?, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) return

        if (tag != null && message != null && tag.contains(USER_PROPERTY_TAG)) {
            val originalTag = tag.removePrefix(USER_PROPERTY_TAG)
            crashlytics.setCustomKey(originalTag, message)
        }

        if (priority == Log.INFO) {
            message?.let { crashlytics.log(it) }
        }

        if (priority == Log.WARN || priority == Log.ERROR) {
            if (t != null) {
                message?.let { crashlytics.log(it) }
                crashlytics.recordException(t)
            } else if (message != null) {
                crashlytics.recordException(Exception(message))
            }
        }

    }

}
