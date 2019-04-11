package com.simprints.id.orchestrator.modals.flows

import android.content.Intent
import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.orchestrator.modals.ModalStepRequest
import com.simprints.id.orchestrator.modals.flows.interfaces.ModalFlow
import com.simprints.id.orchestrator.modals.flows.interfaces.MultiModalFlow
import io.reactivex.Observable

/**
 * Concatenates multi modalities for more complicate flows.
 * @param steps list of ModalFlows to concatenate
 */
class MultiModalFlowBase(private val steps: List<ModalFlow>) : MultiModalFlow {

    override val modalResponses: Observable<ModalResponse> =
        Observable.concat(steps.map { it.modalResponses })

    override val nextModalStepRequest: Observable<ModalStepRequest> =
        Observable.concat(steps.map { it.nextModalStepRequest })

    override fun handleIntentResponse(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        val stepHandler = steps.firstOrNull { it.handleIntentResponse(requestCode, resultCode, data) }
        return stepHandler != null
    }
}
