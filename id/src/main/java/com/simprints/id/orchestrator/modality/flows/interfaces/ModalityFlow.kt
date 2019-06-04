package com.simprints.id.orchestrator.modality.flows.interfaces

import android.content.Intent

/**
 * Representation of a modality flow for a specific modality.
 */
interface ModalityFlow {

    val steps: Map<Int, Step?>

    val nextRequest: Request?

    fun handleIntentResult(requestCode: Int, resultCode: Int, data: Intent?): Response?

    data class Step(val request: Request,
                    var response: Response? = null)

    data class Request(val requestCode: Int,
                       val intent: Intent,
                       var launched: Boolean = false)

    interface Response
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


