package com.simprints.id.orchestrator.modals

import android.content.Intent
import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.orchestrator.modals.flows.ModalStepRequest
import io.reactivex.Observable

class MultiModalFlow(private val steps: ArrayList<ModalFlow>) : ModalFlow {

    override var modalResponses: Observable<ModalResponse> =
        Observable.concat(steps.map { it.modalResponses })

    override var nextIntent: Observable<ModalStepRequest> =
        Observable.concat(steps.map { it.nextIntent })

    override fun handleIntentResponse(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        val stepHandler = steps.firstOrNull { it.handleIntentResponse(requestCode, resultCode, data) }
        return stepHandler != null
    }
}
