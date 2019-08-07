package com.simprints.id.orchestrator.modality.builders

import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.Step

interface AppResponseBuilderForModal {

    fun buildResponse(appRequest: AppRequest,
                      steps: List<Step>,
                      sessionId: String = ""): AppResponse
}
