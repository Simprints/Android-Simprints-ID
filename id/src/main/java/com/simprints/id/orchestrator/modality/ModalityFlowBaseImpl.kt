package com.simprints.id.orchestrator.modality

import com.simprints.id.orchestrator.steps.Step

abstract class ModalityFlowBaseImpl: ModalityFlow {

    override val steps: MutableList<Step> = mutableListOf()

    override fun restoreState(stepsToRestore: List<Step>) {
        steps.clear()
        steps.addAll(stepsToRestore)
    }
}
