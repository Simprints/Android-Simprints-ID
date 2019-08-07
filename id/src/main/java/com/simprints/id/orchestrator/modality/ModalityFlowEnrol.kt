package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.*
import com.simprints.id.orchestrator.steps.canProcessRequestCode
import com.simprints.id.orchestrator.steps.face.FaceEnrolStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintEnrolStepProcessor


class ModalityFlowEnrolImpl(private val fingerprintEnrolStepProcessor: FingerprintEnrolStepProcessor,
                            private val faceEnrolStepProcessor: FaceEnrolStepProcessor) : ModalityFlow {

    override val steps: MutableList<Step> = mutableListOf()

    override fun startFlow(appRequest: AppRequest, modalities: List<Modality>) {
        require(appRequest is AppEnrolRequest)

        modalities.forEach {
            when (it) {
                Modality.FINGER -> fingerprintEnrolStepProcessor.buildStep(appRequest)
                Modality.FACE -> faceEnrolStepProcessor.buildStep(appRequest)
            }.also {
                steps.add(it)
            }
        }
    }

    override fun getLatestOngoingStep(): Step? = steps.firstOrNull { it.status == ONGOING }

    override fun handleIntentResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (fingerprintEnrolStepProcessor.canProcessRequestCode(requestCode)) {
            fingerprintEnrolStepProcessor.processResult(requestCode, resultCode, data)
        } else {
            faceEnrolStepProcessor.processResult(requestCode, resultCode, data)
        }
    }
}
