package com.simprints.id.orchestrator.modality

import com.simprints.id.orchestrator.modality.flows.MultiModalitiesFlowBase
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow

class ModalityFlowBuilderImpl: ModalityFlowFactory {

    override fun buildModalityFlow(steps: List<ModalityFlow>): ModalityFlow =  MultiModalitiesFlowBase(steps)

}
