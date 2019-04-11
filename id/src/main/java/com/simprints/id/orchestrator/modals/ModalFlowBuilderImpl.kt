package com.simprints.id.orchestrator.modals

import com.simprints.id.orchestrator.modals.flows.MultiModalFlowBase
import com.simprints.id.orchestrator.modals.flows.interfaces.ModalFlow

class ModalFlowBuilderImpl: ModalFlowFactory {

    override fun buildModalFlow(steps: List<ModalFlow>): ModalFlow =  MultiModalFlowBase(steps)

}
