package com.simprints.id.orchestrator.modality.builders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow

interface AppResponseFactory {

    fun buildAppResponse(modality: Modality,
                         appRequest: AppRequest,
                         modalitiesResults: List<ModalityFlow.Result>,
                         sessionId: String): AppResponse
}
