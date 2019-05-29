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
 * The specific class needs to implement #getModalityStepRequests and #extractModalityResponse
 */
abstract class SingleModalityFlowBase : SingleModalityFlow {

    abstract val intentRequestCode: Int

    internal lateinit var responsesEmitter: ObservableEmitter<ModalityResponse>
    override val modalityResponses: Observable<ModalityResponse> = Observable.create {
        responsesEmitter = it
    }

    internal lateinit var nextIntentEmitter: ObservableEmitter<ModalityStepRequest>

    override val modalityStepRequests: Observable<ModalityStepRequest> = Observable.create {
        nextIntentEmitter = it
        nextIntentEmitter.onNext(getModalityStepRequests())
    }


    @Throws(IllegalArgumentException::class)
    override fun handleIntentResponse(requestCode: Int, resultCode: Int, data: Intent?): Boolean =
        if (requestCode == intentRequestCode) {
            try {
                val modalityResponse = extractModalityResponse(requestCode, resultCode, data)
                completeWithValidResponse(modalityResponse)

            } catch (t: RuntimeException) {
                t.printStackTrace()
                completeWithAnError(t)
            }
            true
        } else {
            false
        }

    internal fun completeWithAnError(t: Throwable) {
        responsesEmitter.onError(t)
        nextIntentEmitter.onError(t)
    }

    internal fun completeWithValidResponse(modalityResponse: ModalityResponse) {
        responsesEmitter.onNext(modalityResponse)
        responsesEmitter.onComplete()
        nextIntentEmitter.onComplete()
    }

    abstract fun getModalityStepRequests(): ModalityStepRequest
    abstract fun extractModalityResponse(requestCode: Int, resultCode: Int, data: Intent?): ModalityResponse
}
