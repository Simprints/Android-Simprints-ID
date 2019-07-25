package com.simprints.id.orchestrator.modality.builders

import com.simprints.id.domain.modality.ModalityResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse

interface AppResponseBuilderForModal {

    fun buildResponse(appRequest: AppRequest,
                      modalityRespons: List<ModalityResponse>,
                      sessionId: String = ""): AppResponse
}
