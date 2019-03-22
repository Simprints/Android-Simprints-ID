package com.simprints.id.orchestrator.modals

import android.content.Intent
import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.orchestrator.ModalStepRequest
import io.reactivex.Observable

interface ModalFlow {

    var nextIntent: Observable<ModalStepRequest>
    var modalResponses: Observable<ModalResponse>
    fun handleIntentResponse(requestCode: Int, resultCode: Int, data: Intent?)
}
