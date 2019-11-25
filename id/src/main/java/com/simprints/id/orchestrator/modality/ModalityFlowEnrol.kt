package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.core.CoreRequestCode.Companion.isCoreResult
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.Companion.isFaceResult
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode.Companion.isFingerprintResult
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor


class ModalityFlowEnrolImpl(private val fingerprintStepProcessor: FingerprintStepProcessor,
                            private val faceEnrolProcessor: FaceStepProcessor,
                            private val coreStepProcessor: CoreStepProcessor,
                            sessionEventsManager: SessionEventsManager) :
    ModalityFlowBaseImpl(coreStepProcessor, fingerprintStepProcessor, faceEnrolProcessor, sessionEventsManager) {

    override fun startFlow(appRequest: AppRequest, modalities: List<Modality>) {
        require(appRequest is AppEnrolRequest)
        super.startFlow(appRequest, modalities)
        steps.addAll(buildStepsList(modalities))
    }

    private fun buildStepsList(modalities: List<Modality>) =
        modalities.map {
            when (it) {
                Modality.FINGER -> fingerprintStepProcessor.buildStepToCapture()
                Modality.FACE -> faceEnrolProcessor.buildCaptureStep()
            }
        }

    override fun getNextStepToLaunch(): Step? = steps.firstOrNull { it.getStatus() == NOT_STARTED }

    override fun handleIntentResult(appRequest: AppRequest, requestCode: Int, resultCode: Int, data: Intent?): Step? {
        val result = when {
            isCoreResult(requestCode) -> coreStepProcessor.processResult(data)
            isFingerprintResult(requestCode) -> {
                fingerprintStepProcessor.processResult(requestCode, resultCode, data).also {
                    addEventIfFingerprintCaptureResponse(it)
                }
            }
            isFaceResult(requestCode) -> faceEnrolProcessor.processResult(requestCode, resultCode, data)
            else -> throw IllegalStateException("Invalid result from intent")
        }
        completeAllStepsIfExitFormHappened(requestCode, resultCode, data)

        val stepForRequest = steps.firstOrNull { it.requestCode == requestCode }
        return stepForRequest?.apply { setResult(result) }
    }

    private fun addEventIfFingerprintCaptureResponse(it: Step.Result?) {
        if (it is FingerprintCaptureResponse) {
            extractFingerprintAndAddPersonCreationEvent(it)
        }
    }
}
