package com.simprints.id.orchestrator.modality

import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow

interface ModalFlowFactory {

    fun buildModalityFlow(steps: List<ModalityFlow>): ModalityFlow

}
