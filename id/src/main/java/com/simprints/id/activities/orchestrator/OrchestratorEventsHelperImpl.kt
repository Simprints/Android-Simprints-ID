package com.simprints.id.activities.orchestrator

import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.events.callback.*
import com.simprints.id.domain.moduleapi.app.responses.*
import com.simprints.id.tools.TimeHelper

class OrchestratorEventsHelperImpl(private val sessionRepository: SessionRepository,
                                   private val timeHelper: TimeHelper) : OrchestratorEventsHelper {

    override fun addCallbackEventInSessions(appResponse: AppResponse) {
        val callbackEvent = when (appResponse.type) {
            AppResponseType.ENROL -> buildEnrolmentCallbackEvent(appResponse as AppEnrolResponse)
            AppResponseType.IDENTIFY -> buildIdentificationCallbackEvent(appResponse as AppIdentifyResponse)
            AppResponseType.REFUSAL -> buildRefusalCallbackEvent(appResponse as AppRefusalFormResponse)
            AppResponseType.VERIFY -> buildVerificationCallbackEvent(appResponse as AppVerifyResponse)
            AppResponseType.ERROR -> buildErrorCallbackEvent(appResponse as AppErrorResponse)
            AppResponseType.CONFIRMATION -> null
        }

        callbackEvent?.let { sessionRepository.addEventInBackground(it) }
    }

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
}
