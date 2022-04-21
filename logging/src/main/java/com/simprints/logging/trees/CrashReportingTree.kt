package com.simprints.logging.trees

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.simprints.logging.Simber.USER_PROPERTY_TAG
import timber.log.Timber

internal class CrashReportingTree(private val crashlytics: FirebaseCrashlytics) : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) return

        if (tag != null && tag.contains(USER_PROPERTY_TAG)) {
            val originalTag = tag.removePrefix(USER_PROPERTY_TAG)
            crashlytics.setCustomKey(originalTag, message)
        }

        if (priority == Log.INFO) {
            crashlytics.log(message)
        }

        if (priority == Log.WARN || priority == Log.ERROR) {
            if (t != null) {
                crashlytics.log(message)
                crashlytics.recordException(t)
            } else {
                crashlytics.recordException(Exception(message))
            }
        }

    }

}
