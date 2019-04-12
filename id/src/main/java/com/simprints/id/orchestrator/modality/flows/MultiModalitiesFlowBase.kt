package com.simprints.id.orchestrator.modality.flows

import android.content.Intent
import com.simprints.id.domain.modality.ModalityResponse
import com.simprints.id.orchestrator.modality.ModalityStepRequest
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow
import com.simprints.id.orchestrator.modality.flows.interfaces.MultiModalitiesFlow
import io.reactivex.Observable

/**
 * Concatenates multi modalities for more complicate flows.
 * @param steps list of ModalFlows to concatenate
 */
class MultiModalitiesFlowBase(private val steps: List<ModalityFlow>) : MultiModalitiesFlow {

    override val modalityResponses: Observable<ModalityResponse> =
        Observable.concat(steps.map { it.modalityResponses })

    override val nextModalityStepRequest: Observable<ModalityStepRequest> =
        Observable.concat(steps.map { it.nextModalityStepRequest })

    override fun handleIntentResponse(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        val stepHandler = steps.firstOrNull { it.handleIntentResponse(requestCode, resultCode, data) }
        return stepHandler != null
    }
}
