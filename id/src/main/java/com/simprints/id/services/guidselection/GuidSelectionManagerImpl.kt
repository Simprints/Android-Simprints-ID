package com.simprints.id.services.guidselection

import com.simprints.core.tools.extentions.inBackground
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.GuidSelectionEvent
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import com.simprints.id.orchestrator.steps.core.requests.GuidSelectionRequest
import com.simprints.id.tools.time.TimeHelper
import com.simprints.id.tools.ignoreException
import io.reactivex.Completable
import timber.log.Timber

class GuidSelectionManagerImpl(val deviceId: String,
                               val loginInfoManager: LoginInfoManager,
                               val analyticsManager: AnalyticsManager,
                               val crashReportManager: CrashReportManager,
                               private val timerHelper: TimeHelper,
                               val eventRepository: EventRepository) : GuidSelectionManager {

    override suspend fun handleConfirmIdentityRequest(request: GuidSelectionRequest) {
        try {
            checkRequest(request)
            saveGuidSelectionEvent(request)
            reportToAnalytics(request, true)
        } catch (t: Throwable) {
            Timber.e(t)
            crashReportManager.logExceptionOrSafeException(t)
            reportToAnalytics(request, false)
        }
    }

    private fun checkRequest(request: GuidSelectionRequest): Completable = Completable.fromCallable {
        checkProjectId(request.projectId)
    }

    private suspend fun saveGuidSelectionEvent(request: GuidSelectionRequest) =
        ignoreException {
            val event = GuidSelectionEvent(timerHelper.now(), request.selectedGuid)
            inBackground { eventRepository.addEventToCurrentSession(event) }
        }


    private fun reportToAnalytics(request: GuidSelectionRequest, callbackSent: Boolean) =
        analyticsManager.logGuidSelectionWorker(
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            request.sessionId,
            deviceId,
            request.selectedGuid,
            callbackSent)

    private fun checkProjectId(projectId: String) {
        if (!loginInfoManager.isProjectIdSignedIn(projectId)) throw NotSignedInException()
    }
}
