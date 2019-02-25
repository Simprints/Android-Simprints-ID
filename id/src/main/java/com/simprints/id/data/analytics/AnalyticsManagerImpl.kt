package com.simprints.id.data.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.id.data.db.remote.adapters.toFirebaseSession
import com.simprints.id.data.db.remote.models.fb_Session
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.session.Session
import com.simprints.id.session.callout.Callout
import com.simprints.id.tools.extensions.fromLowerCamelToLowerUnderscore
import io.reactivex.Single
import timber.log.Timber
import kotlin.reflect.full.memberProperties

class AnalyticsManagerImpl(private val loginInfoManager: LoginInfoManager,
                           private val preferencesManager: PreferencesManager,
                           private val firebaseAnalytics: FirebaseAnalytics) : AnalyticsManager {

    override val analyticsId: Single<String> = Single.create<String> {
        firebaseAnalytics.appInstanceId.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result
                if (result == null) {
                    it.onError(NullPointerException())
                } else {
                    it.onSuccess(result)
                }
            } else {
                it.onError(task.exception as Throwable)
            }
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

    private fun logUserProperties(userId: String, projectId: String, moduleId: String, deviceId: String) {
        Timber.d("AnalyticsManagerImpl.logUserProperties(userId=$userId, apiKey=$projectId, projectId=$projectId, moduleId=$moduleId, deviceIde=$deviceId)")
        firebaseAnalytics.setUserId(userId)
        firebaseAnalytics.setUserProperty("api_key", projectId)
        firebaseAnalytics.setUserProperty("project_id", projectId)
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
        Timber.d("AnalyticsManagerImpl.logSession(activeSession=$session)")
        val fbSession = session.toFirebaseSession()
        val bundle = Bundle()
        for (property in fb_Session::class.memberProperties) {
            bundle.putString(property.name.fromLowerCamelToLowerUnderscore(), property.get(fbSession).toString())
        }
        firebaseAnalytics.logEvent("activeSession", bundle)
    }
}
