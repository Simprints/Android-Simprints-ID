package com.simprints.id.activities.orchestrator

import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.*
import com.simprints.id.domain.moduleapi.app.responses.*
import com.simprints.id.tools.TimeHelper

class OrchestratorEventsHelperImpl(private val sessionEventsManager: SessionEventsManager,
                                   private val timeHelper: TimeHelper) : OrchestratorEventsHelper {

    override fun addCallbackEventInSessions(appResponse: AppResponse) =
        sessionEventsManager.addEventInBackground(
            when (appResponse.type) {
                AppResponseType.ENROL -> buildEnrolmentCallbackEvent(appResponse as AppEnrolResponse)
                AppResponseType.IDENTIFY -> buildIdentificationCallbackEvent(appResponse as AppIdentifyResponse)
                AppResponseType.REFUSAL -> buildRefusalCallbackEvent(appResponse as AppRefusalFormResponse)
                AppResponseType.VERIFY -> buildVerificationCallbackEvent(appResponse as AppVerifyResponse)
                AppResponseType.CONFIRMATION -> buildConfirmationCallbackEvent(appResponse as AppConfirmationResponse)
                AppResponseType.ERROR -> buildErrorCallbackEvent(appResponse as AppErrorResponse)
            })

    private fun buildEnrolmentCallbackEvent(appResponse: AppEnrolResponse) =
        EnrolmentCallbackEvent(timeHelper.now(), appResponse.guid)

    private fun buildIdentificationCallbackEvent(appResponse: AppIdentifyResponse) =
        with(appResponse) {
            IdentificationCallbackEvent(
                timeHelper.now(),
                sessionId,
                identifications.map {
                    CallbackComparisonScore(it.guidFound, it.confidence, it.tier)
                })
        }

    private fun buildVerificationCallbackEvent(appVerifyResponse: AppVerifyResponse) =
        with(appVerifyResponse.matchingResult) {
            VerificationCallbackEvent(timeHelper.now(),
                CallbackComparisonScore(guidFound, confidence, tier))
        }

    private fun buildRefusalCallbackEvent(appRefusalResponse: AppRefusalFormResponse) =
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
