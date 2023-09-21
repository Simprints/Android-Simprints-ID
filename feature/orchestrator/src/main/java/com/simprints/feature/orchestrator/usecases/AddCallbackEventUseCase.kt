package com.simprints.feature.orchestrator.usecases

import com.simprints.core.ExternalScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.callback.CallbackComparisonScore
import com.simprints.infra.events.event.domain.models.callback.ConfirmationCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.EnrolmentCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.IdentificationCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.RefusalCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.VerificationCallbackEvent
import com.simprints.moduleapi.app.responses.IAppConfirmationResponse
import com.simprints.moduleapi.app.responses.IAppEnrolResponse
import com.simprints.moduleapi.app.responses.IAppErrorResponse
import com.simprints.moduleapi.app.responses.IAppIdentifyResponse
import com.simprints.moduleapi.app.responses.IAppMatchConfidence
import com.simprints.moduleapi.app.responses.IAppMatchResult
import com.simprints.moduleapi.app.responses.IAppRefusalFormResponse
import com.simprints.moduleapi.app.responses.IAppResponse
import com.simprints.moduleapi.app.responses.IAppResponseTier
import com.simprints.moduleapi.app.responses.IAppVerifyResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class AddCallbackEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val timeHelper: TimeHelper,
    @ExternalScope private val externalScope: CoroutineScope
) {

    operator fun invoke(result: IAppResponse) {
        val callbackEvent = when (result) {
            is IAppEnrolResponse -> buildEnrolmentCallbackEvent(result)
            is IAppIdentifyResponse -> buildIdentificationCallbackEvent(result)
            is IAppVerifyResponse -> buildVerificationCallbackEvent(result)
            is IAppRefusalFormResponse -> buildRefusalCallbackEvent(result)
            is IAppErrorResponse -> buildErrorCallbackEvent(result)
            is IAppConfirmationResponse -> buildConfirmIdentityCallbackEvent(result)
            else -> null
        }

        if (callbackEvent != null) {
            externalScope.launch { eventRepository.addOrUpdateEvent(callbackEvent) }
        }
    }

    private fun buildEnrolmentCallbackEvent(appResponse: IAppEnrolResponse) = EnrolmentCallbackEvent(
        timeHelper.now(),
        appResponse.guid
    )

    private fun buildIdentificationCallbackEvent(appResponse: IAppIdentifyResponse) = IdentificationCallbackEvent(
        timeHelper.now(),
        appResponse.sessionId,
        appResponse.identifications.map { buildComparisonScore(it) },
    )

    private fun buildVerificationCallbackEvent(appResponse: IAppVerifyResponse) = VerificationCallbackEvent(
        timeHelper.now(),
        buildComparisonScore(appResponse.matchResult),
    )

    private fun buildComparisonScore(matchResult: IAppMatchResult) = CallbackComparisonScore(
        matchResult.guid,
        matchResult.confidenceScore,
        IAppResponseTier.valueOf(matchResult.tier.name),
        IAppMatchConfidence.valueOf(matchResult.matchConfidence.name),
    )

    private fun buildRefusalCallbackEvent(appResponse: IAppRefusalFormResponse) = RefusalCallbackEvent(
        timeHelper.now(),
        appResponse.reason,
        appResponse.extra,
    )

    private fun buildErrorCallbackEvent(appResponse: IAppErrorResponse) = ErrorCallbackEvent(
        timeHelper.now(),
        ErrorCallbackEvent.ErrorCallbackPayload.Reason.fromAppResponseErrorReasonToEventReason(appResponse.reason),
    )

    private fun buildConfirmIdentityCallbackEvent(appResponse: IAppConfirmationResponse) = ConfirmationCallbackEvent(
        timeHelper.now(),
        appResponse.identificationOutcome,
    )
}
