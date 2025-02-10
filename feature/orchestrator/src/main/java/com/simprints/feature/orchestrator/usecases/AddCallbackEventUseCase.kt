package com.simprints.feature.orchestrator.usecases

import com.simprints.core.SessionCoroutineScope
import com.simprints.core.domain.response.AppMatchConfidence
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.events.event.domain.models.callback.CallbackComparisonScore
import com.simprints.infra.events.event.domain.models.callback.ConfirmationCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.EnrolmentCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.IdentificationCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.RefusalCallbackEvent
import com.simprints.infra.events.event.domain.models.callback.VerificationCallbackEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.orchestration.data.responses.AppConfirmationResponse
import com.simprints.infra.orchestration.data.responses.AppEnrolResponse
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.responses.AppIdentifyResponse
import com.simprints.infra.orchestration.data.responses.AppMatchResult
import com.simprints.infra.orchestration.data.responses.AppRefusalResponse
import com.simprints.infra.orchestration.data.responses.AppResponse
import com.simprints.infra.orchestration.data.responses.AppVerifyResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class AddCallbackEventUseCase @Inject constructor(
    private val eventRepository: SessionEventRepository,
    private val timeHelper: TimeHelper,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) {
    operator fun invoke(result: AppResponse) {
        val callbackEvent = when (result) {
            is AppEnrolResponse -> buildEnrolmentCallbackEvent(result)
            is AppIdentifyResponse -> buildIdentificationCallbackEvent(result)
            is AppVerifyResponse -> buildVerificationCallbackEvent(result)
            is AppConfirmationResponse -> buildConfirmIdentityCallbackEvent(result)
            is AppRefusalResponse -> buildRefusalCallbackEvent(result)
            is AppErrorResponse -> buildErrorCallbackEvent(result)
        }

        sessionCoroutineScope.launch { eventRepository.addOrUpdateEvent(callbackEvent) }
    }

    private fun buildEnrolmentCallbackEvent(appResponse: AppEnrolResponse) = EnrolmentCallbackEvent(
        timeHelper.now(),
        appResponse.guid,
    )

    private fun buildIdentificationCallbackEvent(appResponse: AppIdentifyResponse) = IdentificationCallbackEvent(
        timeHelper.now(),
        appResponse.sessionId,
        appResponse.identifications.map { buildComparisonScore(it) },
    )

    private fun buildVerificationCallbackEvent(appResponse: AppVerifyResponse) = VerificationCallbackEvent(
        timeHelper.now(),
        buildComparisonScore(appResponse.matchResult),
    )

    private fun buildComparisonScore(matchResult: AppMatchResult) = CallbackComparisonScore(
        matchResult.guid,
        matchResult.confidenceScore,
        AppMatchConfidence.valueOf(matchResult.matchConfidence.name),
    )

    private fun buildRefusalCallbackEvent(appResponse: AppRefusalResponse) = RefusalCallbackEvent(
        timeHelper.now(),
        appResponse.reason,
        appResponse.extra,
    )

    private fun buildErrorCallbackEvent(appResponse: AppErrorResponse) = ErrorCallbackEvent(
        timeHelper.now(),
        ErrorCallbackEvent.ErrorCallbackPayload.Reason.fromAppResponseErrorReasonToEventReason(
            appResponse.reason,
        ),
    )

    private fun buildConfirmIdentityCallbackEvent(appResponse: AppConfirmationResponse) = ConfirmationCallbackEvent(
        timeHelper.now(),
        appResponse.identificationOutcome,
    )
}
