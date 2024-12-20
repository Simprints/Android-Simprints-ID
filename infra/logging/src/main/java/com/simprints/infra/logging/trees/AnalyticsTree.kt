package com.simprints.infra.logging.trees

import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.infra.logging.LoggingConstants.AnalyticsUserProperties.USER_ID
import com.simprints.infra.logging.Simber
import timber.log.Timber

internal class AnalyticsTree(
    private val analytics: FirebaseAnalytics,
) : Timber.Tree() {
    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?,
    ) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) return

        if (tag != null && tag.contains(Simber.USER_PROPERTY_TAG)) {
            val originalTag = tag.removePrefix(Simber.USER_PROPERTY_TAG)

            if (originalTag == USER_ID) {
                analytics.setUserId(message)
            } else {
                analytics.setUserProperty(originalTag, message)
            }
        }
    }
}
