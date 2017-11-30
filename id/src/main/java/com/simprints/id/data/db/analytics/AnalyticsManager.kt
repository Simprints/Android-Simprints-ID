package com.simprints.id.data.db.analytics

import com.simprints.id.model.Callout

interface AnalyticsManager {

    fun logException(throwable: Throwable?)

    fun logAlert(alertName: String, apiKey: String, moduleId: String, userId: String,
                 deviceId: String)

    fun logUserProperties(userId: String, apiKey: String, moduleId: String, deviceId: String)

    fun logScannerProperties(macAddress: String, scannerId: String)

    fun logLogin(callout: Callout)

    fun logGuidSelectionService(selectedGuid: String, callbackSent: Boolean,
                                apiKey: String, androidId: String, sessionId: String)
}

