package com.simprints.id.data.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.domain.requests.AppRequest
import io.reactivex.Single
import timber.log.Timber

class AnalyticsManagerImpl(private val loginInfoManager: LoginInfoManager,
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

    override fun logCallout(appRequest: AppRequest) {
        Timber.d("AnalyticsManagerImpl.logCallout(appRequest=$appRequest)")
        with(appRequest) {
            val bundle = Bundle()
            bundle.putString("action", AppRequest.action(appRequest).toString())
            bundle.putString("projectId", appRequest.projectId)
            bundle.putString("userId", appRequest.userId)
            bundle.putString("moduleID", appRequest.moduleId)
            firebaseAnalytics.logEvent("callout", bundle)
        }
    }


    override fun logUserProperties(userId: String, projectId: String, moduleId: String, deviceId: String) {
        Timber.d("AnalyticsManagerImpl.logUserProperties(userId=$userId, apiKey=$projectId, projectId=$projectId, moduleId=$moduleId")
        firebaseAnalytics.setUserId(userId)
        firebaseAnalytics.setUserProperty("api_key", projectId)
        firebaseAnalytics.setUserProperty("project_id", projectId)
        firebaseAnalytics.setUserProperty("module_id", moduleId)
        firebaseAnalytics.setUserProperty("device_id", deviceId)
    }

    override fun logScannerProperties(macAddress: String, scannerId: String) {
        Timber.d("AnalyticsManagerImpl.logScannerProperties(macAddress=$macAddress, lastScannerId=$scannerId)")
        firebaseAnalytics.setUserProperty("mac_address", macAddress)
        firebaseAnalytics.setUserProperty("scanner_id", scannerId)
    }

    override fun logGuidSelectionService(projectId: String, sessionId: String, deviceId: String, selectedGuid: String, callbackSent: Boolean) {
        logGuidSelectionService(projectId, sessionId, selectedGuid, callbackSent, deviceId)
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

    override fun logConnectionStateChange(connected: Boolean, deviceId: String, sessionId: String) {
        logConnectionStateChange(
            connected,
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            deviceId,
            sessionId)
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

    override fun logAuthStateChange(authenticated: Boolean, deviceId: String, sessionId: String) {
        logAuthStateChange(
            authenticated,
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            deviceId,
            sessionId)
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
}
