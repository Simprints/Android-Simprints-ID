package com.simprints.id.data.db.analytics

import com.simprints.id.data.model.CalloutType
import com.simprints.id.data.model.calloutParameters.MainCalloutParameters

interface AnalyticsManager {

    fun logException(throwable: Throwable?)

    fun logNonFatalException(description: String)

    fun logAlert(alertName: String, apiKey: String, moduleId: String, userId: String,
                 deviceId: String)

    fun logMainCalloutParameters(parameters: MainCalloutParameters)

    fun logUserProperties(userId: String, apiKey: String, moduleId: String, deviceId: String)

    fun logScannerProperties(macAddress: String, scannerId: String)

    fun logLogin(calloutType: CalloutType)

    fun logGuidSelectionService(selectedGuid: String, callbackSent: Boolean,
                                apiKey: String, androidId: String, sessionId: String)

    fun logConnectionStateChange(connected: Boolean, apiKey: String,
                                 androidId: String, sessionId: String)

    fun logAuthStateChange(authenticated: Boolean, apiKey: String,
                           androidId: String, sessionId: String)

}

