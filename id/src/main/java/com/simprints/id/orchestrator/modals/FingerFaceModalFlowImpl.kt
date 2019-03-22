package com.simprints.id.orchestrator.modals

import android.content.Intent
import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.orchestrator.ModalStepRequest
import com.simprints.id.orchestrator.modals.ModalFlowIntentRequestCodes.REQUEST_CODE_FACE
import com.simprints.id.orchestrator.modals.ModalFlowIntentRequestCodes.REQUEST_CODE_FINGERPRINT
import io.reactivex.Observable

class FingerFaceModalFlowImpl(private val fingerModalFlow: ModalFlow,
                              private val faceModalFlow: ModalFlow) : ModalFlow {


    override var modalResponses: Observable<ModalResponse> = Observable.concat(fingerModalFlow.modalResponses, faceModalFlow.modalResponses)
    override var nextIntent: Observable<ModalStepRequest> = Observable.concat(fingerModalFlow.nextIntent, faceModalFlow.nextIntent)

    @Throws(IllegalArgumentException::class)
    override fun handleIntentResponse(requestCode: Int, resultCode: Int, data: Intent?) {
        return when (requestCode) {
            REQUEST_CODE_FACE -> faceModalFlow.handleIntentResponse(requestCode, resultCode, data)
            REQUEST_CODE_FINGERPRINT -> fingerModalFlow.handleIntentResponse(requestCode, resultCode, data)
            else -> throw Throwable("Response not recognised")
        }
    }
}
