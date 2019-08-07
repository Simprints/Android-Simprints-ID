package com.simprints.id.activities.orchestrator

import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.*
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.app.DomainToAppResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.*
import com.simprints.id.orchestrator.OrchestratorManager
import com.simprints.id.tools.TimeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OrchestratorViewModel(private val orchestratorManager: OrchestratorManager,
                            private val preferencesManager: PreferencesManager,
                            private val sessionEventsManager: SessionEventsManager,
                            private val timeHelper: TimeHelper) : ViewModel() {

    val nextActivity = orchestratorManager.nextIntent

    val appResponse = Transformations.map(orchestratorManager.appResponse) {
        addCallbackEventInSessions(it)
        DomainToAppResponse.fromDomainToAppResponse(it)
    }

    fun start(appRequest: AppRequest) {
        CoroutineScope(Dispatchers.Main).launch {
            orchestratorManager.start(
                preferencesManager.modalities,
                appRequest,
                sessionEventsManager.getCurrentSession().map { it.id }.blockingGet())
        }
    }

    fun onModalStepRequestDone(requestCode: Int, resultCode: Int, data: Intent?) =
        CoroutineScope(Dispatchers.Main).launch {
            orchestratorManager.onModalStepRequestDone(requestCode, resultCode, data)
        }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun addCallbackEventInSessions(appResponse: AppResponse) =

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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun buildEnrolmentCallbackEvent(appResponse: AppEnrolResponse) =
        EnrolmentCallbackEvent(timeHelper.now(), appResponse.guid)

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun buildIdentificationCallbackEvent(appResponse: AppIdentifyResponse) =
        with(appResponse) {
            IdentificationCallbackEvent(
                timeHelper.now(),
                sessionId,
                identifications.map {
                    CallbackComparisonScore(it.guidFound, it.confidence, it.tier)
                })
        }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun buildVerificationCallbackEvent(appVerifyResponse: AppVerifyResponse) =
        with(appVerifyResponse.matchingResult) {
            VerificationCallbackEvent(timeHelper.now(),
                CallbackComparisonScore(guidFound, confidence, tier))
        }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun buildRefusalCallbackEvent(appRefusalResponse: AppRefusalFormResponse) =
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
