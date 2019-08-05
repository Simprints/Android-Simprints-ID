package com.simprints.id.orchestrator.modality.flows.interfaces

import android.content.Intent

/**
 * Representation of a modality flow for a specific modality.
 */
interface ModalityFlow {

    val steps: List<Step>

    fun handleIntentResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean

    fun getLatestOngoingStep(): Step?

    data class Step(val request: Request,
                    var status: Status) {

        var result: Result? = null
            set(value) {
                field = value
                if (field != null) {
                    status = Status.COMPLETED
                }
            }

        enum class Status {
            ONGOING, COMPLETED
        }
    }

    data class Request(val requestCode: Int,
                       val intent: Intent)

    interface Result
}


/**
 * Represents a single Modality Flow
 * @see com.simprints.id.orchestrator.modality.flows.SingleModalityFlowBase
 */
interface SingleModalityFlow : ModalityFlow

/**
 * Represents a multi Modality Flow.
 * MultiModalitiesFlow can host SingleModalityFlows
 * @see com.simprints.id.orchestrator.modality.flows.MultiModalitiesFlowBase
 */
interface MultiModalitiesFlow : ModalityFlow


