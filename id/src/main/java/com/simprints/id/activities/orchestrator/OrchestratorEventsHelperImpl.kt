package com.simprints.id.activities.orchestrator

import com.simprints.core.tools.extentions.inBackground
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.domain.models.callback.*
import com.simprints.eventsystem.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.Companion.fromAppResponseErrorReasonToEventReason
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse.fromDomainToModuleApiAppErrorResponse
import com.simprints.id.domain.moduleapi.app.responses.*
import com.simprints.moduleapi.app.responses.IAppResponseTier

class OrchestratorEventsHelperImpl(
    private val eventRepository: com.simprints.eventsystem.event.EventRepository,
    private val timeHelper: TimeHelper
) : OrchestratorEventsHelper {

    override fun addCallbackEventInSessions(appResponse: AppResponse) {
        val callbackEvent = when (appResponse.type) {
            AppResponseType.ENROL -> buildEnrolmentCallbackEvent(appResponse as AppEnrolResponse)
            AppResponseType.IDENTIFY -> buildIdentificationCallbackEvent(appResponse as AppIdentifyResponse)
            AppResponseType.REFUSAL -> buildRefusalCallbackEvent(appResponse as AppRefusalFormResponse)
            AppResponseType.VERIFY -> buildVerificationCallbackEvent(appResponse as AppVerifyResponse)
            AppResponseType.ERROR -> buildErrorCallbackEvent(appResponse as AppErrorResponse)
            AppResponseType.CONFIRMATION -> buildConfirmIdentityCallbackEvent(appResponse as AppConfirmationResponse)
        }

        callbackEvent.let {
            inBackground {
                eventRepository.addOrUpdateEvent(it)
            }
        }
    }

    private fun buildEnrolmentCallbackEvent(appResponse: AppEnrolResponse) =
        EnrolmentCallbackEvent(timeHelper.now(), appResponse.guid)

    private fun buildIdentificationCallbackEvent(appResponse: AppIdentifyResponse) =
        with(appResponse) {
            IdentificationCallbackEvent(
                timeHelper.now(),
                sessionId,
                identifications.map {
                    CallbackComparisonScore(
                        it.guidFound,
                        it.confidence,
                        IAppResponseTier.valueOf(it.tier.name)
                    )
                })
        }

    private fun buildVerificationCallbackEvent(appVerifyResponse: AppVerifyResponse) =
        with(appVerifyResponse.matchingResult) {
            VerificationCallbackEvent(
                timeHelper.now(),
                CallbackComparisonScore(guidFound, confidence, IAppResponseTier.valueOf(tier.name))
            )
        }

    private fun buildRefusalCallbackEvent(appRefusalResponse: AppRefusalFormResponse) =
        with(appRefusalResponse) {
            RefusalCallbackEvent(
                timeHelper.now(),
                answer.reason.name,
                answer.optionalText
            )
        }

    private fun buildErrorCallbackEvent(appErrorResponse: AppErrorResponse) =
        ErrorCallbackEvent(
            timeHelper.now(),
            fromAppResponseErrorReasonToEventReason(
                fromDomainToModuleApiAppErrorResponse(appErrorResponse).reason
            )
        )

    private fun buildConfirmIdentityCallbackEvent(appResponse: AppConfirmationResponse) =
        ConfirmationCallbackEvent(timeHelper.now(), appResponse.identificationOutcome)
}
