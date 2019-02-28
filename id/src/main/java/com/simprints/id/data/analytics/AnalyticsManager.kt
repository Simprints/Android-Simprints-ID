package com.simprints.id.data.analytics

import com.simprints.id.domain.requests.AppRequest
import io.reactivex.Single


interface AnalyticsManager {

    val analyticsId: Single<String>

    fun logCallout(idRequest: AppRequest)

    fun logUserProperties(userId: String, projectId: String, moduleId: String, deviceId: String)
    fun logGuidSelectionService(projectId: String, sessionId: String, deviceId: String, selectedGuid: String, callbackSent: Boolean)
    fun logConnectionStateChange(connected: Boolean, deviceId: String, sessionId: String)
    fun logAuthStateChange(authenticated: Boolean, deviceId: String, sessionId: String)
    fun logScannerProperties(macAddress: String, scannerId: String)
}
