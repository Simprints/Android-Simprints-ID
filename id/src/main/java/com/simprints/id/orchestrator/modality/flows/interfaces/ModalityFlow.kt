package com.simprints.id.orchestrator.modality.flows.interfaces

import android.content.Intent
import com.simprints.id.domain.modality.ModalityResponse
import com.simprints.id.orchestrator.modality.ModalityStepRequest
import io.reactivex.Observable

/**
 * Representation of a modality flow for a specific modality.
 */
interface ModalityFlow {

    val nextModalityStepRequest: Observable<ModalityStepRequest>
    val modalityResponses: Observable<ModalityResponse>
    fun handleIntentResponse(requestCode: Int, resultCode: Int, data: Intent?): Boolean
}

/**
 * Represents a single Modality Flow
 * @see com.simprints.id.orchestrator.modality.flows.SingleModalityFlowBase
 */
interface SingleModalityFlow: ModalityFlow

/**
 * Represents a multi Modality Flow.
 * MultiModalitiesFlow can host SingleModalityFlows
 * @see com.simprints.id.orchestrator.modality.flows.MultiModalitiesFlowBase
 */
interface MultiModalitiesFlow: ModalityFlow
