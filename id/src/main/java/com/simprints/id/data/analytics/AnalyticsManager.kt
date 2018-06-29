package com.simprints.id.data.analytics

import com.simprints.id.exceptions.safe.SimprintsException
import com.simprints.id.exceptions.unsafe.SimprintsError
import com.simprints.id.session.Session
import com.simprints.id.session.callout.Callout


interface AnalyticsManager {

    fun logThrowable(throwable: Throwable)

    fun logError(error: SimprintsError)

    fun logSafeException(exception: SimprintsException)

    fun logAlert(alertName: String, apiKey: String, moduleId: String, userId: String,
                 deviceId: String)

    fun logCallout(callout: Callout)

    fun logUserProperties(userId: String, apiKey: String, moduleId: String, deviceId: String)

    fun logScannerProperties(macAddress: String, scannerId: String)

    fun logGuidSelectionService(apiKey: String, sessionId: String, selectedGuid: String,
                                callbackSent: Boolean, androidId: String)

    fun logConnectionStateChange(connected: Boolean, apiKey: String,
                                 androidId: String, sessionId: String)

    fun logAuthStateChange(authenticated: Boolean, apiKey: String,
                           androidId: String, sessionId: String)

    fun logSession(session: Session)

}
