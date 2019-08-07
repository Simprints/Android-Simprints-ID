package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.face.FaceVerifyStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.BaseFingerprintStepProcessor.Companion.isFingerprintResult
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintVerifyStepProcessor


class ModalityFlowVerifyImpl(private val fingerprintVerifyStepProcessor: FingerprintVerifyStepProcessor,
                             private val faceVerifyStepProcessor: FaceVerifyStepProcessor) : ModalityFlow {

    override val steps: MutableList<Step> = mutableListOf()

    override fun startFlow(appRequest: AppRequest, modalities: List<Modality>) {
        require(appRequest is AppVerifyRequest)

        steps.addAll(buildStepsList(appRequest, modalities))
    }

    private fun buildStepsList(appRequest: AppVerifyRequest, modalities: List<Modality>) =
        modalities.map {
            when (it) {
                Modality.FINGER -> fingerprintVerifyStepProcessor.buildStep(appRequest)
                Modality.FACE -> faceVerifyStepProcessor.buildStep(appRequest)
            }
        }

    override fun getNextStepToStart(): Step? = steps.firstOrNull { it.status == NOT_STARTED }

    override fun handleIntentResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = if (isFingerprintResult(requestCode)) {
            fingerprintVerifyStepProcessor.processResult(requestCode, resultCode, data)
        } else {
            faceVerifyStepProcessor.processResult(requestCode, resultCode, data)
        }

        val stepForRequest = steps.firstOrNull { it.request.requestCode == requestCode }
        stepForRequest?.result = result
    }
}
