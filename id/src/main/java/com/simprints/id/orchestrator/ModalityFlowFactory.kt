package com.simprints.id.orchestrator

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.modality.ModalityFlow

interface ModalityFlowFactory {

    fun createModalityFlow(appRequest: AppRequest,
                           modalities: List<Modality>): ModalityFlow

}
