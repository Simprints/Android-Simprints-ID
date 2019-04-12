package com.simprints.id.orchestrator.modals.builders

import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse

interface AppResponseBuilderForModal {

    fun buildResponse(appRequest: AppRequest,
                      modalResponses: List<ModalResponse>,
                      sessionId: String = ""): AppResponse
}
