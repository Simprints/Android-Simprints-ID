package com.simprints.id.orchestrator.responsebuilders

import com.simprints.core.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.infra.config.domain.models.GeneralConfiguration

/**
 * It creates a final AppResponse based on the results of the ModalityFlow steps.
 */
interface AppResponseFactory {

    suspend fun buildAppResponse(modalities: List<GeneralConfiguration.Modality>,
                                 appRequest: AppRequest,
                                 steps: List<Step>,
                                 sessionId: String): AppResponse
}
