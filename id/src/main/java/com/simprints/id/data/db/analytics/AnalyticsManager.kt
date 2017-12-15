package com.simprints.id.data.db.analytics

import com.simprints.id.data.model.CalloutType
import com.simprints.id.data.model.calloutParameters.MainCalloutParameters


interface AnalyticsManager {

    fun logError(error: Error)

    fun logAlert(alertName: String, apiKey: String, moduleId: String, userId: String,
                 deviceId: String)

    fun logSafeException(exception: RuntimeException)

    fun logMainCalloutParameters(parameters: MainCalloutParameters)

    fun logUserProperties(userId: String, apiKey: String, moduleId: String, deviceId: String)

    fun logScannerProperties(macAddress: String, scannerId: String)

    fun logLogin(calloutType: CalloutType)

    fun logGuidSelectionService(apiKey: String, sessionId: String, selectedGuid: String,
                                callbackSent: Boolean, androidId: String)

    fun logConnectionStateChange(connected: Boolean, apiKey: String,
                                 androidId: String, sessionId: String)

    fun logAuthStateChange(authenticated: Boolean, apiKey: String,
                           androidId: String, sessionId: String)

}
