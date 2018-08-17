package com.simprints.id.data.analytics

import android.os.Bundle
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.id.data.db.remote.adapters.toFirebaseSession
import com.simprints.id.data.db.remote.models.fb_Session
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.ALERT_TYPE
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

class AnalyticsManagerImpl(private val loginInfoManager: LoginInfoManager,
                           private val preferencesManager: PreferencesManager,
                           private val firebaseAnalytics: FirebaseAnalytics) : AnalyticsManager {

    override fun logAlert(alertType: ALERT_TYPE) {
        logAlert(
            alertType.name,
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            preferencesManager.moduleId,
            loginInfoManager.getSignedInUserIdOrEmpty(),
            preferencesManager.deviceId)
    }

    private fun logAlert(alertName: String, apiKey: String, moduleId: String, userId: String,
                         deviceId: String) {
        Timber.d("AnalyticsManagerImpl.logAlert(alertName=$alertName, ...)")
        logAlertToCrashlytics(alertName)
        logAlertToFirebaseAnalytics(alertName, apiKey, moduleId, userId, deviceId)
    }

    private fun logAlertToCrashlytics(alertName: String) {
        Timber.d("AnalyticsManagerImpl.logAlertToCrashlytics(alertName=$alertName)")
        if (Fabric.isInitialized()) {
            Crashlytics.log(alertName)
        }
    }

    private fun logAlertToFirebaseAnalytics(alertName: String, apiKey: String, moduleId: String,
                                            userId: String, deviceId: String) {
        Timber.d("AnalyticsManagerImpl.logAlertToFirebaseAnalytics(alertName=$alertName, ...)")
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
        Timber.d("AnalyticsManagerImpl.logSafeException(description=$exception)")
        val bundle = Bundle()
        bundle.putString("exception", exception.toString())
        bundle.putString("description", exception.message)
        firebaseAnalytics.logEvent("safe_exception", bundle)
    }

    private fun logUnsafeThrowable(e: Throwable) {
        Timber.e(e)
        if (Fabric.isInitialized()) {
            Crashlytics.logException(e)
        }
    }

    override fun logCallout(callout: Callout) {
        Timber.d("AnalyticsManagerImpl.logCallout(callout=$callout)")
        with(callout) {
            val bundle = Bundle()
            bundle.putString("action", action.toString())
            for (calloutParameter in parameters) {
                bundle.putString(calloutParameter.key, calloutParameter.value.toString())
            }
            firebaseAnalytics.logEvent("callout", bundle)
        }
    }

    override fun logUserProperties() {
        logUserProperties(
            loginInfoManager.getSignedInUserIdOrEmpty(),
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            preferencesManager.moduleId,
            preferencesManager.deviceId)
    }

    private fun logUserProperties(userId: String, project_id: String, moduleId: String, deviceId: String) {
        Timber.d("AnalyticsManagerImpl.logUserProperties(userId=$userId, project_id=$project_id,moduleId=$moduleId, deviceIde=$deviceId)")
        firebaseAnalytics.setUserId(userId)
        firebaseAnalytics.setUserProperty("project_id", project_id)
        firebaseAnalytics.setUserProperty("module_id", moduleId)
        firebaseAnalytics.setUserProperty("device_id", deviceId)
    }

    override fun logScannerProperties() {
        logScannerProperties(
            preferencesManager.macAddress,
            preferencesManager.scannerId)
    }

    private fun logScannerProperties(macAddress: String, scannerId: String) {
        Timber.d("AnalyticsManagerImpl.logScannerProperties(macAddress=$macAddress, scannerId=$scannerId)")
        firebaseAnalytics.setUserProperty("mac_address", macAddress)
        firebaseAnalytics.setUserProperty("scanner_id", scannerId)
    }

    override fun logGuidSelectionService(projectId: String, sessionId: String, selectedGuid: String, callbackSent: Boolean) {
        logGuidSelectionService(projectId, sessionId, selectedGuid, callbackSent, preferencesManager.deviceId)
    }

    private fun logGuidSelectionService(apiKey: String, sessionId: String,
                                        selectedGuid: String, callbackSent: Boolean, androidId: String) {
        Timber.d("AnalyticsManagerImpl.logGuidSelectionService(selectedGuid=$selectedGuid, callbackSent=$callbackSent)")
        val bundle = Bundle()
        bundle.putString("api_key", apiKey)
        bundle.putString("selected_guid", selectedGuid)
        bundle.putString("android_id", androidId)
        bundle.putString("session_id", sessionId)
        bundle.putBoolean("callback_sent", callbackSent)
        firebaseAnalytics.logEvent("guid_selection_service", bundle)
    }

    override fun logConnectionStateChange(connected: Boolean) {
        logConnectionStateChange(
            connected,
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            preferencesManager.deviceId,
            preferencesManager.sessionId)
    }

    private fun logConnectionStateChange(connected: Boolean, apiKey: String,
                                         androidId: String, sessionId: String) {
        Timber.d("AnalyticsManagerImpl.logConnectionStateChange(connected=$connected)")
        val bundle = Bundle()
        bundle.putString("api_key", apiKey)
        bundle.putString("android_id", androidId)
        bundle.putString("session_id", sessionId)
        bundle.putBoolean("connected", connected)
        firebaseAnalytics.logEvent("connection_state_change", bundle)
    }

    override fun logAuthStateChange(authenticated: Boolean) {
        logAuthStateChange(
            authenticated,
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            preferencesManager.deviceId,
            preferencesManager.sessionId)
    }

    private fun logAuthStateChange(authenticated: Boolean, apiKey: String, androidId: String, sessionId: String) {
        Timber.d("AnalyticsManagerImpl.logAuthStateChange(authenticated=$authenticated)")
        val bundle = Bundle()
        bundle.putString("api_key", apiKey)
        bundle.putString("android_id", androidId)
        bundle.putString("session_id", sessionId)
        bundle.putBoolean("authenticated", authenticated)
        firebaseAnalytics.logEvent("auth_state_change", bundle)
    }

    override fun logSession(session: Session) {
        Timber.d("AnalyticsManagerImpl.logSession(session=$session)")
        val fbSession = session.toFirebaseSession()
        val bundle = Bundle()
        for (property in fb_Session::class.memberProperties) {
            bundle.putString(property.name.fromLowerCamelToLowerUnderscore(), property.get(fbSession).toString())
        }
        firebaseAnalytics.logEvent("session", bundle)
    }
}
