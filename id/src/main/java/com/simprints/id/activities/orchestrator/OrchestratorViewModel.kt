package com.simprints.id.activities.orchestrator

import android.content.Intent
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.*
import com.simprints.id.domain.moduleapi.app.DomainToAppResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.*
import com.simprints.id.orchestrator.OrchestratorManager
import com.simprints.id.services.scheduledSync.SyncSchedulerHelper
import com.simprints.id.tools.TimeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OrchestratorViewModel(val appRequest: AppRequest,
                            val orchestratorManager: OrchestratorManager,
                            val sessionEventsManager: SessionEventsManager,
                            val syncSchedulerHelper: SyncSchedulerHelper,
                            val timeHelper: TimeHelper) : ViewModel() {

    val nextActivity = orchestratorManager.nextIntent

    val appResponse = Transformations.map(orchestratorManager.appResponse) {
        addCallbackEventInSessions(it)
        DomainToAppResponse.fromDomainToAppResponse(it)
    }

    fun start() {
        CoroutineScope(Dispatchers.Main).launch {
            orchestratorManager.initOrchestrator(appRequest, sessionEventsManager.getCurrentSession().map { it.id }.blockingGet())
        }
    }

    fun onModalStepRequestDone(requestCode: Int, resultCode: Int, data: Intent?) =
        CoroutineScope(Dispatchers.Main).launch {
            orchestratorManager.onModalStepRequestDone(requestCode, resultCode, data)
        }

    internal fun addCallbackEventInSessions(appResponse: AppResponse) =

        sessionEventsManager.updateSession { session ->

            when (appResponse.type) {
                AppResponseType.ENROL -> buildEnrolmentCallbackEvent(appResponse as AppEnrolResponse)
                AppResponseType.IDENTIFY -> buildIdentificationCallbackEvent(appResponse as AppIdentifyResponse)
                AppResponseType.REFUSAL -> buildRefusalCallbackEvent(appResponse as AppRefusalFormResponse)
                AppResponseType.VERIFY -> buildVerificationCallbackEvent(appResponse as AppVerifyResponse)
                AppResponseType.CONFIRMATION -> buildConfirmationCallbackEvent(appResponse as AppConfirmationResponse)
                AppResponseType.ERROR -> buildErrorCallbackEvent(appResponse as AppErrorResponse)
            }.let {
                session.addEvent(it)
            }
        }

    internal fun buildEnrolmentCallbackEvent(appResponse: AppEnrolResponse) =
        EnrolmentCallbackEvent(timeHelper.now(), appResponse.guid)

    internal fun buildIdentificationCallbackEvent(appResponse: AppIdentifyResponse) =
        with(appResponse) {
            IdentificationCallbackEvent(
                timeHelper.now(),
                sessionId,
                identifications.map {
                    CallbackComparisonScore(it.guidFound, it.confidence, it.tier)
                })
        }

    internal fun buildVerificationCallbackEvent(appVerifyResponse: AppVerifyResponse) =
        with(appVerifyResponse.matchingResult) {
            VerificationCallbackEvent(timeHelper.now(),
                CallbackComparisonScore(guidFound, confidence, tier))
        }

    internal fun buildRefusalCallbackEvent(appRefusalResponse: AppRefusalFormResponse) =
        with(appRefusalResponse) {
            RefusalCallbackEvent(
                timeHelper.now(),
                answer.reason.name,
                answer.optionalText)
        }

    private fun buildErrorCallbackEvent(appErrorResponse: AppErrorResponse) =
        ErrorCallbackEvent(timeHelper.now(), appErrorResponse.reason)

    private fun buildConfirmationCallbackEvent(appConfirmationResponse: AppConfirmationResponse) =
        ConfirmationCallbackEvent(
            timeHelper.now(),
            appConfirmationResponse.identificationOutcome
        )
}
