package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.Companion.isFaceResult
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode.Companion.isFingerprintResult
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor


class ModalityFlowEnrolImpl(private val fingerprintStepProcessor: FingerprintStepProcessor,
                            private val faceEnrolProcessor: FaceStepProcessor,
                            private val coreStepProcessor: CoreStepProcessor) : ModalityFlowBaseImpl() {

    override fun startFlow(appRequest: AppRequest, modalities: List<Modality>) {
        require(appRequest is AppEnrolRequest)
        with(appRequest) {
            steps.addAll(buildCoreStepsList(projectId, userId, moduleId, metadata))
            steps.addAll(buildStepsList(this, modalities))
        }
    }

    private fun buildStepsList(appRequest: AppEnrolRequest, modalities: List<Modality>) =
        modalities.map {
            with(appRequest) {
                when (it) {
                    Modality.FINGER -> fingerprintStepProcessor.buildStepEnrol(projectId, userId, moduleId, metadata)
                    Modality.FACE -> faceEnrolProcessor.buildStepEnrol(projectId, userId, moduleId)
                }
            }
        }

    override fun getNextStepToLaunch(): Step? = steps.firstOrNull { it.status == NOT_STARTED }

    override fun handleIntentResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = when {
            isFingerprintResult(requestCode) -> fingerprintStepProcessor.processResult(requestCode, resultCode, data)
            isFaceResult(requestCode) -> faceEnrolProcessor.processResult(requestCode, resultCode, data)
            else -> {
                coreStepProcessor.processResult(requestCode, data)
            }
        }

        val stepForRequest = steps.firstOrNull { it.requestCode == requestCode }
        stepForRequest?.result = result
    }

    private fun buildCoreStepsList(projectId: String, userId: String, moduleId: String, metadata: String) =
        coreStepProcessor.buildStepEnrolOrIdentify(projectId, userId, moduleId, metadata)
}
