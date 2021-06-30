package com.simprints.logging

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber


internal class AnalyticsTree(private val analytics: FirebaseAnalytics) : Timber.Tree() {

    companion object{
        // If for some reason there is no tag for the event
        private const val defaultTag = "DEFAULT"
    }

    override fun log(priority: Int, tag: String?, message: String?, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) return

        if (tag != null && message != null && tag.contains(Simber.USER_PROPERTY_TAG)) {
            val originalTag = tag.removePrefix(Simber.USER_PROPERTY_TAG)
            analytics.setUserProperty(originalTag, message)
        }

        if (priority == Log.INFO) {

            message?.let {
                val params = Bundle()
                params.putString(tag ?: defaultTag, it)
                analytics.logEvent(tag ?: defaultTag, params)
            }

        }

    }
}
