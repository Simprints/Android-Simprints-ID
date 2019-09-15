package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.Companion.isFaceResult
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode.Companion.isFingerprintResult
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor

class ModalityFlowIdentifyImpl(private val fingerprintStepProcessor: FingerprintStepProcessor,
                               private val faceStepProcessor: FaceStepProcessor,
                               private val coreStepProcessor: CoreStepProcessor) : ModalityFlowBaseImpl(coreStepProcessor) {

    override val steps: MutableList<Step> = mutableListOf()

    override fun startFlow(appRequest: AppRequest, modalities: List<Modality>) {
        require(appRequest is AppIdentifyRequest)
        super.startFlow(appRequest, modalities)
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

    override fun getNextStepToLaunch(): Step? =
        steps.firstOrNull { it.getStatus() == NOT_STARTED }

    override fun handleIntentResult(requestCode: Int, resultCode: Int, data: Intent?): Step? {
        val result = when {
            isFingerprintResult(requestCode) -> fingerprintStepProcessor.processResult(requestCode, resultCode, data)
            isFaceResult(requestCode) -> faceStepProcessor.processResult(requestCode, resultCode, data)
            else -> coreStepProcessor.processResult(requestCode, data)
        }

        val stepForRequest = steps.firstOrNull { it.requestCode == requestCode }
        return stepForRequest?.also { it.result = result }
    }
}
