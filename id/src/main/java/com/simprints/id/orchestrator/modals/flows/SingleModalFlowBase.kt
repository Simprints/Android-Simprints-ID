package com.simprints.id.orchestrator.modals.flows

import android.content.Intent
import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.orchestrator.modals.ModalStepRequest
import com.simprints.id.orchestrator.modals.flows.interfaces.SingleModalFlow
import io.reactivex.Observable
import io.reactivex.ObservableEmitter

/**
 * Generic class for a ModalFlow with a single action.
 * It completes immediately after the first step completes.
 */
abstract class SingleModalFlowBase : SingleModalFlow {

    abstract val intentRequestCode: Int

    private lateinit var responsesEmitter: ObservableEmitter<ModalResponse>
    override var modalResponses: Observable<ModalResponse> = Observable.create {
        responsesEmitter = it
    }

    private lateinit var nextIntentEmitter: ObservableEmitter<ModalStepRequest>
    override var nextIntent: Observable<ModalStepRequest> = Observable.create {
        nextIntentEmitter = it
        nextIntentEmitter.onNext(getNextIntent())
    }


    @Throws(IllegalArgumentException::class)
    override fun handleIntentResponse(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        var intentHandled = false
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
            intentHandled = true
        }

        return intentHandled
    }

    abstract fun getNextIntent(): ModalStepRequest
    abstract fun extractModalResponse(requestCode: Int, resultCode: Int, data: Intent?): ModalResponse
}
