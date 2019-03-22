package com.simprints.id.orchestrator.modals

import android.content.Intent
import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.orchestrator.ModalStepRequest
import io.reactivex.Observable

class FaceFingerModalFlowImpl(private val fingerModalFlow: ModalFlow,
                              private val faceModalFlow: ModalFlow) : ModalFlow {


    override var modalResponses: Observable<ModalResponse> = Observable.concat(faceModalFlow.modalResponses, fingerModalFlow.modalResponses)
    override var nextIntent: Observable<ModalStepRequest> = Observable.concat(faceModalFlow.nextIntent, fingerModalFlow.nextIntent)

    @Throws(IllegalArgumentException::class)
    override fun handleIntentResponse(requestCode: Int, resultCode: Int, data: Intent?) {
        return when (requestCode) {
            ModalFlowIntentRequestCodes.REQUEST_CODE_FACE -> faceModalFlow.handleIntentResponse(requestCode, resultCode, data)
            ModalFlowIntentRequestCodes.REQUEST_CODE_FINGERPRINT -> fingerModalFlow.handleIntentResponse(requestCode, resultCode, data)
            else -> throw Throwable("Response not recognised")
        }
    }
}
