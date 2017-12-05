package com.simprints.id.data.db.analytics

import android.os.Bundle
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.id.data.model.CalloutType
import com.simprints.id.data.model.calloutParameters.MainCalloutParameters
import timber.log.Timber

/**
 * Something to keep in mind about Firebase Analytics:
 * "Generally, events logged by your app are batched together over the period of approximately
 * one hour and uploaded together. This approach conserves the battery on end users’ devices
 * and reduces network data usage."
 */

class FirebaseAnalyticsManager(private val firebaseAnalytics: FirebaseAnalytics): AnalyticsManager {

    override fun logException(throwable: Throwable?) {
        Timber.d("FirebaseAnalyticsManager.logException(throwable=$throwable)")
        Crashlytics.logException(throwable)
    }

    override fun logNonFatalException(description: String) {
        Timber.d("FirebaseAnalyticsManager.logNonFatalException(description=$description")
        Crashlytics.log(description)
        val bundle = Bundle()
        bundle.putString("description", description)
        firebaseAnalytics.logEvent("non_fatal_exception", bundle)
    }

    override fun logAlert(alertName: String, apiKey: String, moduleId: String, userId: String,
                          deviceId: String) {
        Timber.d("FirebaseAnalyticsManager.logAlert(alertName=$alertName, ...)")
        logAlertToCrashlytics(alertName)
        logAlertToFirebaseAnalytics(alertName, apiKey, moduleId, userId, deviceId)
    }

    private fun logAlertToCrashlytics(alertName: String) {
        Timber.d("FirebaseAnalyticsManager.logAlertToCrashlytics(alertName=$alertName)")
        Crashlytics.log(alertName)
    }

    // TODO: Do we have to log things like api_key, user_id, etc to every firebase event? Or is it enough to log it once, and then we can link everything togeter in big query requests?
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

    override fun logMainCalloutParameters(parameters: MainCalloutParameters) {
        Timber.d("FirebaseAnalyticsManager.logMainCalloutParameters(parameters=$parameters)")
        with(parameters) {
            val bundle = Bundle()
            bundle.putString("type", typeParameter.value.intentAction)
            bundle.putString("api_key", apiKeyParameter.value)
            bundle.putString("module_id", moduleIdParameter.value)
            bundle.putString("user_id", userIdParameter.value)
            bundle.putString("update_id", patientIdParameter.updateIdParameter.value)
            bundle.putString("verify_id", patientIdParameter.verifyIdParameter.value)
            bundle.putString("calling_package", callingPackageParameter.value)
            bundle.putString("metadata", metadataParameter.value)
            bundle.putString("result_format", resultFormatParameter.value)
            bundle.putString("unexpected_parameters", unexpectedParameters.value.toString())
            firebaseAnalytics.logEvent("main_callout_parameters", bundle)
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

    override fun logLogin(calloutType: CalloutType) {
        Timber.d("FirebaseAnalyticsManager.logLogin(calloutType=$calloutType)")
        val bundle = Bundle()
        bundle.putString("calloutType", calloutType.name)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
    }

    override fun logGuidSelectionService(selectedGuid: String, callbackSent: Boolean,
                                         apiKey: String, androidId: String, sessionId: String) {
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
}