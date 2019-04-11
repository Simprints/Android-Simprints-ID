package com.simprints.id.orchestrator.modals

import com.simprints.id.orchestrator.modals.flows.interfaces.ModalFlow

interface ModalFlowFactory {

    fun buildModalFlow(steps: List<ModalFlow>): ModalFlow

}
