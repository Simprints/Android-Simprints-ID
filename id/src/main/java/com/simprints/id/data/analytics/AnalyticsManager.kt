package com.simprints.id.data.analytics

import com.simprints.id.domain.requests.IdRequest
import io.reactivex.Single


interface AnalyticsManager {

    val analyticsId: Single<String>

    fun logCallout(idRequest: IdRequest)
    fun logUserProperties()
    fun logScannerProperties()
    fun logGuidSelectionService(projectId: String, sessionId: String, selectedGuid: String, callbackSent: Boolean)
    fun logConnectionStateChange(connected: Boolean)
    fun logAuthStateChange(authenticated: Boolean)
}
