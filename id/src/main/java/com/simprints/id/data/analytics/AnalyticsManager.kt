package com.simprints.id.data.analytics

import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.exceptions.safe.SimprintsException
import com.simprints.id.exceptions.unsafe.SimprintsError
import com.simprints.id.services.scheduledSync.peopleDownSync.models.PeopleDownSyncTrigger
import com.simprints.id.session.Session
import com.simprints.id.session.callout.Callout
import com.simprints.libsimprints.FingerIdentifier
import io.reactivex.Single


interface AnalyticsManager {

    val analyticsId: Single<String>

    fun logThrowable(throwable: Throwable)
    fun logException(error: SimprintsError)
    fun logSafeException(exception: SimprintsException)

    fun logInfo(analyticsTag: AnalyticsTags, logPrompter: LogPrompter, message: String)
    fun logWarning(analyticsTag: AnalyticsTags, logPrompter: LogPrompter, message: String)


    fun logCallout(callout: Callout)
    fun logAlert(alertType: ALERT_TYPE)
    fun logUserProperties()
    fun logScannerProperties()
    fun logGuidSelectionService(projectId: String, sessionId: String, selectedGuid: String, callbackSent: Boolean)
    fun logConnectionStateChange(connected: Boolean)
    fun logAuthStateChange(authenticated: Boolean)
    fun logSession(session: Session)

    fun setProjectIdCrashlyticsKey(projectId: String)
    fun setUserIdCrashlyticsKey(userId: String)
    fun setModuleIdsCrashlyticsKey(moduleIds: Set<String>?)
    fun setDownSyncTriggersCrashlyticsKey(peopleDownSyncTriggers: Map<PeopleDownSyncTrigger, Boolean>)
    fun setSessionIdCrashlyticsKey(sessionId: String)
    fun setFingersSelectedCrashlyticsKey(fingersSelected: Map<FingerIdentifier, Boolean>)
}
