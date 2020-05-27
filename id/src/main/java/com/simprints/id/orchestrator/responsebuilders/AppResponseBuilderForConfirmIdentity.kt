package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppConfirmationResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.response.GuidSelectionResponse

class AppResponseBuilderForConfirmIdentity : BaseAppResponseBuilder() {

    override suspend fun buildAppResponse(modalities: List<Modality>,
                                          appRequest: AppRequest,
                                          steps: List<Step>,
                                          sessionId: String): AppResponse {

        val results = steps.map { it.getResult() }
        val guildSelectionResponse = getCoreResponseForGuidSelection(results)
        return AppConfirmationResponse(guildSelectionResponse.identificationOutcome)
    }

    private fun getCoreResponseForGuidSelection(results: List<Step.Result?>): GuidSelectionResponse =
        results.filterIsInstance(GuidSelectionResponse::class.java).lastOrNull() ?: throw Throwable("GuidSelectionResponse responses not found")
}

