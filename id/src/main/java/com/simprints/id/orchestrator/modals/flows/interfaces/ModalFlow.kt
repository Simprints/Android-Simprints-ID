package com.simprints.id.orchestrator.modals.flows.interfaces

import android.content.Intent
import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.orchestrator.modals.ModalStepRequest
import io.reactivex.Observable

/**
 * Representation of a modal flow for a specific modality.
 */
interface ModalFlow {

    val nextModalStepRequest: Observable<ModalStepRequest>
    val modalResponses: Observable<ModalResponse>
    fun handleIntentResponse(requestCode: Int, resultCode: Int, data: Intent?): Boolean
}

/**
 * Represents a single Modal Flow
 * @see com.simprints.id.orchestrator.modals.flows.SingleModalFlowBase
 */
interface SingleModalFlow: ModalFlow

/**
 * Represents a multi Modal Flow.
 * MultiModalFlow can host single SingleModalFlow
 * @see com.simprints.id.orchestrator.modals.flows.MultiModalFlowBase
 */
interface MultiModalFlow: ModalFlow
