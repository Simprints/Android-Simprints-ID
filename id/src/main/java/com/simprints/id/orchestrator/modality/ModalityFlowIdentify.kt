package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessorImpl.Companion.isFingerprintResult
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED

class ModalityFlowIdentifyImpl(private val fingerprintStepProcessor: FingerprintStepProcessor,
                               private val faceStepProcessor: FaceStepProcessor) : ModalityFlow {

    override val steps: MutableList<Step> = mutableListOf()

    override fun startFlow(appRequest: AppRequest, modalities: List<Modality>) {
        require(appRequest is AppIdentifyRequest)

        steps.addAll(buildStepsList(appRequest, modalities))
    }

    private fun buildStepsList(appRequest: AppIdentifyRequest, modalities: List<Modality>) =
        modalities.map {
            with(appRequest) {
                when (it) {
                    Modality.FINGER -> fingerprintStepProcessor.buildStepIdentify(projectId, userId, moduleId, metadata)
                    Modality.FACE -> faceStepProcessor.buildStepIdentify(projectId, userId, moduleId)
                }
            }
        }

    override fun getNextStepToStart(): Step? =
        steps.firstOrNull { it.status == NOT_STARTED }


    override fun handleIntentResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = if (isFingerprintResult(requestCode)) {
            fingerprintStepProcessor.processResult(requestCode, resultCode, data)
        } else {
            faceStepProcessor.processResult(requestCode, resultCode, data)
        }

        val stepForRequest = steps.firstOrNull { it.requestCode == requestCode }
        stepForRequest?.result = result
    }
}
