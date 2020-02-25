package com.simprints.id.services

import com.simprints.id.data.db.session.AnalyticsManager
import com.simprints.id.data.db.session.crashreport.CrashReportManager
import com.simprints.id.data.db.session.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.session.eventdata.models.domain.events.callout.ConfirmationCalloutEvent
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.domain.moduleapi.app.requests.AppIdentityConfirmationRequest
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import com.simprints.id.tools.TimeHelper
import io.reactivex.Completable

class GuidSelectionManagerImpl(val deviceId: String,
                               val loginInfoManager: LoginInfoManager,
                               val analyticsManager: AnalyticsManager,
                               val crashReportManager: CrashReportManager,
                               private val timerHelper: TimeHelper,
                               val sessionEventsManager: SessionEventsManager) : GuidSelectionManager {

    override fun handleIdentityConfirmationRequest(request: AppIdentityConfirmationRequest): Completable =
        addConfirmationCalloutEvent(request)
            .andThen(checkRequest(request))
            .andThen(saveGuidSelectionEvent(request))
            .doOnError {
                it.printStackTrace()
                crashReportManager.logExceptionOrSafeException(it)
                reportToAnalytics(request, false)
            }
            .doOnComplete { reportToAnalytics(request, true) }

    private fun checkRequest(request: AppIdentityConfirmationRequest): Completable = Completable.fromCallable {
        checkProjectId(request.projectId)
    }

    private fun addConfirmationCalloutEvent(request: AppIdentityConfirmationRequest) =
        sessionEventsManager.addEvent(ConfirmationCalloutEvent(
            timerHelper.now(),
            request.projectId,
            request.selectedGuid,
            request.sessionId))


    private fun saveGuidSelectionEvent(request: AppIdentityConfirmationRequest): Completable =
        sessionEventsManager
            .addGuidSelectionEvent(request.selectedGuid, request.sessionId)

    private fun reportToAnalytics(request: AppIdentityConfirmationRequest, callbackSent: Boolean) =
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
