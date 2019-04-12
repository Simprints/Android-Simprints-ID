package com.simprints.id.orchestrator.modality.flows

import android.content.Intent
import com.simprints.id.domain.modality.ModalityResponse
import com.simprints.id.orchestrator.modality.ModalityStepRequest
import com.simprints.id.orchestrator.modality.flows.interfaces.SingleModalityFlow
import io.reactivex.Observable
import io.reactivex.ObservableEmitter

/**
 * Generic class for a single modality flows such as Fingerprint or Face.
 * It requests only a step (see #nextIntentEmitter) and emits only a response (see #nextIntentEmitter).
 * The specific class needs to implement #getNextModalityStepRequest and #extractModalityResponse
 */
abstract class SingleModalityFlowBase : SingleModalityFlow {

    abstract val intentRequestCode: Int

    private lateinit var responsesEmitter: ObservableEmitter<ModalityResponse>
    override val modalityResponses: Observable<ModalityResponse> = Observable.create {
        responsesEmitter = it
    }

    private lateinit var nextIntentEmitter: ObservableEmitter<ModalityStepRequest>
    override val nextModalityStepRequest: Observable<ModalityStepRequest> = Observable.create {
        nextIntentEmitter = it
        nextIntentEmitter.onNext(getNextModalityStepRequest())
    }


    @Throws(IllegalArgumentException::class)
    override fun handleIntentResponse(requestCode: Int, resultCode: Int, data: Intent?): Boolean =
        if (requestCode == intentRequestCode) {
            try {
                val potentialModalResponse = extractModalityResponse(requestCode, resultCode, data)

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

    abstract fun getNextModalityStepRequest(): ModalityStepRequest
    abstract fun extractModalityResponse(requestCode: Int, resultCode: Int, data: Intent?): ModalityResponse
}
