package com.simprints.id.orchestrator.builders

import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.steps.Step

interface AppResponseBuilderForModal {

    fun buildResponse(appRequest: AppRequest,
                      steps: List<Step>,
                      sessionId: String = ""): AppResponse
}
