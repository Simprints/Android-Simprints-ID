package com.simprints.id.orchestrator.modality

import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow

interface ModalityFlowFactory {

    fun buildModalityFlow(steps: List<ModalityFlow>): ModalityFlow
}
