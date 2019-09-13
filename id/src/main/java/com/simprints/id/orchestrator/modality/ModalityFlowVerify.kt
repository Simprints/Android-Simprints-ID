package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.Companion.isFaceResult
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode.Companion.isFingerprintResult
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor

class ModalityFlowVerifyImpl(private val fingerprintStepProcessor: FingerprintStepProcessor,
                             private val faceStepProcessor: FaceStepProcessor,
                             coreStepProcessor: CoreStepProcessor) : ModalityFlowBaseImpl(coreStepProcessor) {

    override fun startFlow(appRequest: AppRequest, modalities: List<Modality>) {
        require(appRequest is AppVerifyRequest)
        super.startFlow(appRequest, modalities)
        steps.addAll(buildStepsList(appRequest, modalities))
    }

    private fun buildStepsList(appRequest: AppVerifyRequest, modalities: List<Modality>) =
        modalities.map {
            with(appRequest) {
                when (it) {
                    FINGER -> fingerprintStepProcessor.buildStepVerify(projectId, userId, projectId, metadata, verifyGuid)
                    FACE -> faceStepProcessor.buildStepVerify(projectId, userId, projectId)
                }
            }
        }

    override fun getNextStepToLaunch(): Step? = steps.firstOrNull { it.status == NOT_STARTED }

    override fun handleIntentResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = when {
            isFingerprintResult(requestCode) -> fingerprintStepProcessor.processResult(requestCode, resultCode, data)
            isFaceResult(requestCode) -> faceStepProcessor.processResult(requestCode, resultCode, data)
            else -> super.processResult(resultCode = resultCode, data = data)
        }

        val stepForRequest = steps.firstOrNull { it.requestCode == requestCode }
        stepForRequest?.result = result
    }
}
