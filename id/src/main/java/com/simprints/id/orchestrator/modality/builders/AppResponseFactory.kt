package com.simprints.id.orchestrator.modality.builders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.ModalityResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse

interface AppResponseFactory {

    fun buildAppResponse(modality: Modality,
                         appRequest: AppRequest,
                         modalityResponses: List<ModalityResponse>,
                         sessionId: String): AppResponse
}
