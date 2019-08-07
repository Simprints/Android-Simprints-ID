package com.simprints.id.orchestrator.modality.steps.fingerprint

import android.content.Intent
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.face.FaceToDomainResponse
import com.simprints.id.domain.moduleapi.fingerprint.DomainToFingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory
import com.simprints.id.orchestrator.modality.steps.Step
import com.simprints.id.orchestrator.modality.steps.Step.Result
import com.simprints.id.orchestrator.modality.steps.Step.Status.ONGOING
import com.simprints.id.orchestrator.modality.steps.StepProcessor
import com.simprints.moduleapi.face.responses.IFaceResponse
import com.simprints.moduleapi.face.responses.IFaceResponse.Companion.BUNDLE_KEY

interface FingerprintVerifyStepProcessor : StepProcessor {
    fun buildStep(verifyRequest: AppVerifyRequest): Step
}

class FingerprintVerifyStepProcessorImpl(private val fingerprintRequestFactory: FingerprintRequestFactory,
                                         private val prefs: PreferencesManager,
                                         private val packageName: String) : BaseFingerprintStepProcessor(), FingerprintVerifyStepProcessor {

    override val requestCode = FINGERPRINT_REQUEST_CODE + 3

    override fun buildStep(verifyRequest: AppVerifyRequest): Step {
        val fingerprintRequest = fingerprintRequestFactory.buildFingerprintRequest(verifyRequest, prefs)
        val intent = buildIntent(DomainToFingerprintRequest.fromDomainToFingerprintRequest(fingerprintRequest), packageName)
        return Step(intent, ONGOING)
    }

    override fun processResult(requestCode: Int, resultCode: Int, data: Intent?): Result? =
        data?.getParcelableExtra<IFaceResponse>(BUNDLE_KEY)?.let {
            FaceToDomainResponse.fromFaceToDomainResponse(it)
        }
}
