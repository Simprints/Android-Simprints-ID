package com.simprints.id.services

import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.domain.moduleapi.app.requests.AppIdentityConfirmationRequest
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import io.reactivex.Completable

class GuidSelectionManagerImpl(val deviceId: String,
                               val loginInfoManager: LoginInfoManager,
                               val analyticsManager: AnalyticsManager,
                               val crashReportManager: CrashReportManager,
                               val sessionEventsManager: SessionEventsManager) : GuidSelectionManager {

    override fun saveGUIDSelection(request: AppIdentityConfirmationRequest): Completable =
        checkRequest(request)
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

    private fun saveGuidSelectionEvent(request: AppIdentityConfirmationRequest): Completable =
        sessionEventsManager
            .addGuidSelectionEvent(request.selectedGuid, request.sessionId)

    private fun reportToAnalytics(request: AppIdentityConfirmationRequest, callbackSent: Boolean) =
        analyticsManager.logGuidSelectionService(
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            request.sessionId,
            deviceId,
            request.selectedGuid,
            callbackSent)

    private fun checkProjectId(projectId: String) {
        if (!loginInfoManager.isProjectIdSignedIn(projectId)) throw NotSignedInException()
    }
}
