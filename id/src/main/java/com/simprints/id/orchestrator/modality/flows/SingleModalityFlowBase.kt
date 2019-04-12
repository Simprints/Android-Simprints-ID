package com.simprints.id.orchestrator.modals.flows

import android.content.Intent
import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.orchestrator.modals.ModalStepRequest
import com.simprints.id.orchestrator.modals.flows.interfaces.SingleModalFlow
import io.reactivex.Observable
import io.reactivex.ObservableEmitter

/**
 * Generic class for a single modality flows such as Fingerprint or Face.
 * It requests only a step (see #nextIntentEmitter) and emits only a response (see #nextIntentEmitter).
 * The specific class needs to implement #getNextModalStepRequest and #extractModalResponse
 */
abstract class SingleModalFlowBase : SingleModalFlow {

    abstract val intentRequestCode: Int

    private lateinit var responsesEmitter: ObservableEmitter<ModalResponse>
    override val modalResponses: Observable<ModalResponse> = Observable.create {
        responsesEmitter = it
    }

    private lateinit var nextIntentEmitter: ObservableEmitter<ModalStepRequest>
    override val nextModalStepRequest: Observable<ModalStepRequest> = Observable.create {
        nextIntentEmitter = it
        nextIntentEmitter.onNext(getNextModalStepRequest())
    }


    @Throws(IllegalArgumentException::class)
    override fun handleIntentResponse(requestCode: Int, resultCode: Int, data: Intent?): Boolean =
        if (requestCode == intentRequestCode) {
            try {
                val potentialModalResponse = extractModalResponse(requestCode, resultCode, data)

                responsesEmitter.onNext(potentialModalResponse)
                responsesEmitter.onComplete()
                nextIntentEmitter.onComplete()

            } catch (t: Throwable) {
                t.printStackTrace()
                responsesEmitter.onError(t)
                nextIntentEmitter.onError(t)
            }
            true
        } else {
            false
        }

    abstract fun getNextModalStepRequest(): ModalStepRequest
    abstract fun extractModalResponse(requestCode: Int, resultCode: Int, data: Intent?): ModalResponse
}
