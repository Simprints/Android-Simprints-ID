package com.simprints.id.orchestrator.responsebuilders

import com.simprints.core.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.steps.Step

interface AppResponseBuilder {

    suspend fun buildAppResponse(modalities: List<Modality>,
                                 appRequest: AppRequest,
                                 steps: List<Step>,
                                 sessionId: String): AppResponse
}
