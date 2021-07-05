package com.simprints.id.data.analytics

import com.simprints.id.domain.moduleapi.app.requests.AppRequest


interface AnalyticsManager {

    fun logCallout(appRequest: AppRequest.AppRequestFlow)

    fun logUserProperties(userId: String, projectId: String, moduleId: String, deviceId: String)
    fun logGuidSelectionWorker(projectId: String, sessionId: String, deviceId: String, selectedGuid: String, callbackSent: Boolean)
    fun logConnectionStateChange(connected: Boolean, deviceId: String, sessionId: String)
    fun logAuthStateChange(authenticated: Boolean, deviceId: String, sessionId: String)
    fun logScannerProperties(macAddress: String, scannerId: String)
}
