package com.simprints.id.data.analytics

import android.os.Bundle
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.id.data.db.remote.adapters.toFirebaseSession
import com.simprints.id.data.db.remote.models.fb_Session
import com.simprints.id.exceptions.safe.SimprintsException
import com.simprints.id.exceptions.unsafe.SimprintsError
import com.simprints.id.session.Session
import com.simprints.id.session.callout.Callout
import com.simprints.id.tools.extensions.fromLowerCamelToLowerUnderscore
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import kotlin.reflect.full.memberProperties

/**
 * Something to keep in mind about Firebase Analytics:
 * "Generally, events logged by your app are batched together over the period of approximately
 * one hour and uploaded together. This approach conserves the battery on end users’ devices
 * and reduces network data usage."
 */

class FirebaseAnalyticsManager(private val firebaseAnalytics: FirebaseAnalytics) : AnalyticsManager {

    override fun logAlert(alertName: String, apiKey: String, moduleId: String, userId: String,
                          deviceId: String) {
        Timber.d("FirebaseAnalyticsManager.logAlert(alertName=$alertName, ...)")
        logAlertToCrashlytics(alertName)
        logAlertToFirebaseAnalytics(alertName, apiKey, moduleId, userId, deviceId)
    }

    private fun logAlertToCrashlytics(alertName: String) {
        Timber.d("FirebaseAnalyticsManager.logAlertToCrashlytics(alertName=$alertName)")
        if (Fabric.isInitialized()) {
            Crashlytics.log(alertName)
        }
    }

    // TODO: Do we have to log things like api_key, user_id, etc to every firebase event? Or is it enough to log it once, and then we can link everything together in big query requests?
    private fun logAlertToFirebaseAnalytics(alertName: String, apiKey: String, moduleId: String,
                                            userId: String, deviceId: String) {
        Timber.d("FirebaseAnalyticsManager.logAlertToFirebaseAnalytics(alertName=$alertName, ...)")
        val bundle = Bundle()
        bundle.putString("alert_name", alertName)
        bundle.putString("api_key", apiKey)
        bundle.putString("module_id", moduleId)
        bundle.putString("user_id", userId)
        bundle.putString("device_id", deviceId)
        firebaseAnalytics.logEvent("alert", bundle)
    }

    override fun logThrowable(throwable: Throwable) =
        when (throwable) {
            is SimprintsError -> logError(throwable)
            is SimprintsException -> logSafeException(throwable)
            else -> logUnexpectedThrowable(throwable)
        }

    private fun logUnexpectedThrowable(throwable: Throwable) {
        logUnsafeThrowable(throwable)
    }

    override fun logError(error: SimprintsError) {
        logUnsafeThrowable(error)
    }

    override fun logSafeException(exception: SimprintsException) {
        Timber.d("FirebaseAnalyticsManager.logSafeException(description=$exception)")
        val bundle = Bundle()
        bundle.putString("exception", exception.toString())
        bundle.putString("description", exception.message)
        firebaseAnalytics.logEvent("safe_exception", bundle)
    }

    private fun logUnsafeThrowable(e: Throwable) {
        Timber.d(e)
        Crashlytics.logException(e)
    }

    override fun logCallout(callout: Callout) {
        Timber.d("FirebaseAnalyticsManager.logCallout(callout=$callout)")
        with(callout) {
            val bundle = Bundle()
            bundle.putString("action", action.toString())
            for (calloutParameter in parameters) {
                bundle.putString(calloutParameter.key, calloutParameter.value.toString())
            }
            firebaseAnalytics.logEvent("callout", bundle)
        }
    }

    override fun logUserProperties(userId: String, apiKey: String, moduleId: String, deviceId: String) {
        Timber.d("FirebaseAnalyticsManager.logUserProperties(userId=$userId, apiKey=$apiKey,moduleId=$moduleId, deviceIde=$deviceId)")
        firebaseAnalytics.setUserId(userId)
        firebaseAnalytics.setUserProperty("api_key", apiKey)
        firebaseAnalytics.setUserProperty("module_id", moduleId)
        firebaseAnalytics.setUserProperty("device_id", deviceId)
    }

    override fun logScannerProperties(macAddress: String, scannerId: String) {
        Timber.d("FirebaseAnalyticsManager.logScannerProperties(macAddress=$macAddress, scannerId=$scannerId)")
        firebaseAnalytics.setUserProperty("mac_address", macAddress)
        firebaseAnalytics.setUserProperty("scanner_id", scannerId)
    }

    override fun logGuidSelectionService(apiKey: String, sessionId: String,
                                         selectedGuid: String, callbackSent: Boolean, androidId: String) {
        Timber.d("FirebaseAnalyticsManager.logGuidSelectionService(selectedGuid=$selectedGuid, callbackSent=$callbackSent)")
        val bundle = Bundle()
        bundle.putString("api_key", apiKey)
        bundle.putString("selected_guid", selectedGuid)
        bundle.putString("android_id", androidId)
        bundle.putString("session_id", sessionId)
        bundle.putBoolean("callback_sent", callbackSent)
        firebaseAnalytics.logEvent("guid_selection_service", bundle)
    }

    override fun logConnectionStateChange(connected: Boolean, apiKey: String,
                                                  androidId: String, sessionId: String) {
        Timber.d("FirebaseAnalyticsManager.logConnectionStateChange(connected=$connected)")
        val bundle = Bundle()
        bundle.putString("api_key", apiKey)
        bundle.putString("android_id", androidId)
        bundle.putString("session_id", sessionId)
        bundle.putBoolean("connected", connected)
        firebaseAnalytics.logEvent("connection_state_change", bundle)
    }

    override fun logAuthStateChange(authenticated: Boolean, apiKey: String, androidId: String, sessionId: String) {
        Timber.d("FirebaseAnalyticsManager.logAuthStateChange(authenticated=$authenticated)")
        val bundle = Bundle()
        bundle.putString("api_key", apiKey)
        bundle.putString("android_id", androidId)
        bundle.putString("session_id", sessionId)
        bundle.putBoolean("authenticated", authenticated)
        firebaseAnalytics.logEvent("auth_state_change", bundle)
    }

    override fun logSession(session: Session) {
        Timber.d("FirebaseAnalyticsManager.logSession(session=$session)")
        val fbSession = session.toFirebaseSession()
        val bundle = Bundle()
        for (property in fb_Session::class.memberProperties) {
            bundle.putString(property.name.fromLowerCamelToLowerUnderscore(), property.get(fbSession).toString())
        }
        firebaseAnalytics.logEvent("session", bundle)
    }
}
