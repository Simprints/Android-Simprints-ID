package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.steps.Step

interface AppResponseBuilder {

    fun buildAppResponse(modalities: List<Modality>,
                         appRequest: AppRequest,
                         steps: List<Step>,
                         sessionId: String): AppResponse
}
