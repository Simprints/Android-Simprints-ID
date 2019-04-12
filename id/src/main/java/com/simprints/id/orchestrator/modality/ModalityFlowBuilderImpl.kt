package com.simprints.id.orchestrator.modality

import com.simprints.id.orchestrator.modality.flows.MultiModalityFlowBase
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow

class ModalFlowBuilderImpl: ModalFlowFactory {

    override fun buildModalFlow(steps: List<ModalityFlow>): ModalityFlow =  MultiModalityFlowBase(steps)

}
