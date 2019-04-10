package com.simprints.id.orchestrator.modals.flows.interfaces

import android.content.Intent
import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.orchestrator.modals.ModalStepRequest
import io.reactivex.Observable

/**
 * Representation of the flow for a specific modality.
 * It can be either a single step only (see SingleModalFlow) or a more complicate
 * and generic one (see MultiModalFlowBase)
 */
interface ModalFlow {

    var nextIntent: Observable<ModalStepRequest>
    var modalResponses: Observable<ModalResponse>
    fun handleIntentResponse(requestCode: Int, resultCode: Int, data: Intent?): Boolean
}

interface SingleModalFlow: ModalFlow
interface MultiModalFlow: ModalFlow
