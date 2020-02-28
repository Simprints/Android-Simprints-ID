package com.simprints.id.services

import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.events.callout.ConfirmationCalloutEvent
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.domain.moduleapi.app.requests.AppIdentityConfirmationRequest
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import com.simprints.id.tools.TimeHelper
import io.reactivex.Completable
import timber.log.Timber

class GuidSelectionManagerImpl(val deviceId: String,
                               val loginInfoManager: LoginInfoManager,
                               val analyticsManager: AnalyticsManager,
                               val crashReportManager: CrashReportManager,
                               private val timerHelper: TimeHelper,
                               val sessionRepository: SessionRepository) : GuidSelectionManager {

    override suspend fun handleIdentityConfirmationRequest(request: AppIdentityConfirmationRequest) {
        try {
            addConfirmationCalloutEvent(request)
            checkRequest(request)
            saveGuidSelectionEvent(request)
            reportToAnalytics(request, true)
        } catch (t: Throwable) {
            Timber.e(t)
            crashReportManager.logExceptionOrSafeException(t)
            reportToAnalytics(request, false)
        }
    }

    private fun checkRequest(request: AppIdentityConfirmationRequest): Completable = Completable.fromCallable {
        checkProjectId(request.projectId)
    }

    private fun addConfirmationCalloutEvent(request: AppIdentityConfirmationRequest) =
        sessionRepository.addEventToCurrentSessionInBackground(ConfirmationCalloutEvent(
            timerHelper.now(),
            request.projectId,
            request.selectedGuid,
            request.sessionId))


    private suspend fun saveGuidSelectionEvent(request: AppIdentityConfirmationRequest) =
        sessionRepository
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
