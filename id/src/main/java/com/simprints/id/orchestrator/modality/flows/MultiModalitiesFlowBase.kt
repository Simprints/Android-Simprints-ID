package com.simprints.id.orchestrator.modality.flows

import android.content.Intent
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow.*
import com.simprints.id.orchestrator.modality.flows.interfaces.MultiModalitiesFlow
import timber.log.Timber
import java.lang.IllegalArgumentException

/**
 * Concatenates multi modalities for more complicate flows.
 * @param steps list of ModalFlows to concatenate
 */
class MultiModalitiesFlowBase(private val modalitiesFlows: List<ModalityFlow>) : MultiModalitiesFlow {

    override val steps: Map<Int, Step?>
        get() = linkedMapOf<Int, Step?>().apply {
            modalitiesFlows.forEach {
                this.putAll(it.steps)
            }
        }

    override val nextRequest: Request?
        get() =
            modalitiesFlows.firstOrNull {
                it.nextRequest != null
            }?.nextRequest.also {
                Timber.d("TEST_TEST: next Intent: $it")
            }


    override fun handleIntentResult(requestCode: Int, resultCode: Int, data: Intent?): Response {
        return try {
            modalitiesFlows.mapNotNull {
                it.handleIntentResult(requestCode, resultCode, data)
            }.first()

        } catch (t: Throwable) {
            t.printStackTrace()
            throw IllegalArgumentException("Impossbile to process response") //StopShip:
        }
    }
}
