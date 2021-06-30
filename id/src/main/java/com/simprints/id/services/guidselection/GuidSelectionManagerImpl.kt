package com.simprints.id.services.guidselection

import com.simprints.core.analytics.CrashReportManager
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.tools.extentions.inBackground
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.domain.models.GuidSelectionEvent
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import com.simprints.id.orchestrator.steps.core.requests.GuidSelectionRequest
import com.simprints.id.tools.ignoreException
import com.simprints.logging.Simber

class GuidSelectionManagerImpl(val deviceId: String,
                               val loginInfoManager: LoginInfoManager,
                               val analyticsManager: AnalyticsManager,
                               val crashReportManager: CrashReportManager,
                               private val timerHelper: TimeHelper,
                               val eventRepository: com.simprints.eventsystem.event.EventRepository
) : GuidSelectionManager {

    override suspend fun handleConfirmIdentityRequest(request: GuidSelectionRequest) {
        try {
            checkRequest(request)
            saveGuidSelectionEvent(request)
            reportToAnalytics(request, true)
        } catch (t: Throwable) {
            Simber.d(t)
            crashReportManager.logExceptionOrSafeException(t)
            reportToAnalytics(request, false)
        }
    }

    private fun checkRequest(request: GuidSelectionRequest) {
        if (!loginInfoManager.isProjectIdSignedIn(request.projectId)) throw NotSignedInException()
    }

    private suspend fun saveGuidSelectionEvent(request: GuidSelectionRequest) =
        ignoreException {
            val event = GuidSelectionEvent(timerHelper.now(), request.selectedGuid)
            inBackground { eventRepository.addOrUpdateEvent(event) }
        }


    private fun reportToAnalytics(request: GuidSelectionRequest, callbackSent: Boolean) =
        analyticsManager.logGuidSelectionWorker(
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            request.sessionId,
            deviceId,
            request.selectedGuid,
            callbackSent)
}
