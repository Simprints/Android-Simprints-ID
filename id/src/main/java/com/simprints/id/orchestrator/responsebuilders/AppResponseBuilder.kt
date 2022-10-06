package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.infra.config.domain.models.GeneralConfiguration

interface AppResponseBuilder {

    suspend fun buildAppResponse(
        modalities: List<GeneralConfiguration.Modality>,
        appRequest: AppRequest,
        steps: List<Step>,
        sessionId: String
    ): AppResponse
}
