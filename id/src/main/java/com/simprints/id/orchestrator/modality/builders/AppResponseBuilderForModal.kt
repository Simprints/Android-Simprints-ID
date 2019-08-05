package com.simprints.id.orchestrator.modality.builders

import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow

interface AppResponseBuilderForModal {

    fun buildResponse(appRequest: AppRequest,
                      modalityRespons: List<ModalityFlow.Result>,
                      sessionId: String = ""): AppResponse
}
