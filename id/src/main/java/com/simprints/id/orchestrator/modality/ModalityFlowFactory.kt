package com.simprints.id.orchestrator.modality

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow

interface ModalityFlowFactory {

    fun buildModalityFlow(appRequest: AppRequest, modality: Modality): ModalityFlow

}
