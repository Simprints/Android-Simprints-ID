package com.simprints.id.data.analytics

import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.exceptions.safe.SafeException
import com.simprints.id.exceptions.unexpected.UnexpectedException
import com.simprints.id.session.Session
import com.simprints.id.session.callout.Callout
import io.reactivex.Single


interface AnalyticsManager {

    val analyticsId: Single<String>

    fun logThrowable(throwable: Throwable)
    fun logError(error: UnexpectedException)
    fun logSafeException(exception: SafeException)

    fun logCallout(callout: Callout)
    fun logAlert(alertType: ALERT_TYPE)
    fun logUserProperties()
    fun logScannerProperties()
    fun logGuidSelectionService(projectId: String, sessionId: String, selectedGuid: String, callbackSent: Boolean)
    fun logConnectionStateChange(connected: Boolean)
    fun logAuthStateChange(authenticated: Boolean)
    fun logSession(session: Session)
}
