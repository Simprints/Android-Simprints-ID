package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.canProcessRequestCode
import com.simprints.id.orchestrator.steps.face.FaceIdentifyStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintIdentifyStepProcessor


class ModalityFlowIdentifyImpl(private val fingerprintIdentifyStepProcessor: FingerprintIdentifyStepProcessor,
                               private val faceIdentifyStepProcessor: FaceIdentifyStepProcessor) : ModalityFlow {

    override val steps: MutableList<Step> = mutableListOf()

    override fun startFlow(appRequest: AppRequest, modalities: List<Modality>) {
        require(appRequest is AppIdentifyRequest)

        steps.addAll(buildStepsList(appRequest, modalities))
    }

    private fun buildStepsList(appRequest: AppIdentifyRequest, modalities: List<Modality>) =
        modalities.map {
            when (it) {
                Modality.FINGER -> fingerprintIdentifyStepProcessor.buildStep(appRequest)
                Modality.FACE -> faceIdentifyStepProcessor.buildStep(appRequest)
            }
        }

    override fun getNextStepToStart(): Step? =
        steps.firstOrNull { it.status == NOT_STARTED }


    override fun handleIntentResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = if (fingerprintIdentifyStepProcessor.canProcessRequestCode(requestCode)) {
            fingerprintIdentifyStepProcessor.processResult(requestCode, resultCode, data)
        } else {
            faceIdentifyStepProcessor.processResult(requestCode, resultCode, data)
        }

        val stepForRequest = steps.firstOrNull { it.request.requestCode == requestCode }
        stepForRequest?.result = result
    }
}
