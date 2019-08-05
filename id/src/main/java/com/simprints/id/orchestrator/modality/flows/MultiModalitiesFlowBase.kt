package com.simprints.id.orchestrator.modality.flows

import android.content.Intent
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow.Step
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow.Step.Status.ONGOING
import com.simprints.id.orchestrator.modality.flows.interfaces.MultiModalitiesFlow

/**
 * Concatenates multi modalities for more complicate flows.
 * @param steps list of ModalFlows to concatenate
 */
class MultiModalitiesFlowBase(private val modalitiesFlows: List<ModalityFlow>) : MultiModalitiesFlow {

    override val steps: List<Step>
        get() = modalitiesFlows.map { it.steps }.flatten()

    @Throws(IllegalArgumentException::class)
    override fun handleIntentResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean =
        modalitiesFlows.find { it.handleIntentResult(requestCode, resultCode, data) } != null

    override fun getLatestOngoingStep(): Step? =
        steps.firstOrNull { it.status == ONGOING }
}
