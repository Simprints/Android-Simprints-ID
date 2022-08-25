package com.simprints.id.orchestrator

import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.modality.ModalityFlow

interface ModalityFlowFactory {

    suspend fun createModalityFlow(appRequest: AppRequest): ModalityFlow

}
