package com.simprints.id.orchestrator.modals

import com.simprints.id.orchestrator.modals.flows.interfaces.ModalFlow

interface ModalFlowBuilder {

    fun buildModalFlow(steps: List<ModalFlow>): ModalFlow

}
