package com.simprints.id.data.db.analytics

import android.os.Bundle
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Something to keep in mind about Firebase Analytics:
 * "Generally, events logged by your app are batched together over the period of approximately
 * one hour and uploaded together. This approach conserves the battery on end users’ devices
 * and reduces network data usage."
 */

class FirebaseAnalyticsManager(private val firebaseAnalytics: FirebaseAnalytics): AnalyticsManager {

    override fun logException(throwable: Throwable?) {
        Crashlytics.logException(throwable)
    }

    // TODO: Do we have to log things like api_key, user_id, etc to every firebase event? Or is it enough to log it once, and then we can link everything togeter in big query requests?
    override fun logAlert(alertName: String,
                          apiKey: String,
                          moduleId: String,
                          userId: String,
                          deviceId: String) {
        Crashlytics.log(alertName)

        val bundle = Bundle()
        bundle.putString("alert_name", alertName)
        bundle.putString("api_key", apiKey)
        bundle.putString("module_id", moduleId)
        bundle.putString("user_id", userId)
        bundle.putString("device_id", deviceId)
        firebaseAnalytics.logEvent("alert", bundle)
    }

}