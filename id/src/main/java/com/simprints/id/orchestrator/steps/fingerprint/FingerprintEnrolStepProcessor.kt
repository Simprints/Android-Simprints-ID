package com.simprints.id.orchestrator.modality.steps.fingerprint

import android.content.Intent
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.face.FaceToDomainResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceResponse
import com.simprints.id.domain.moduleapi.fingerprint.DomainToFingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory
import com.simprints.id.orchestrator.modality.steps.Step
import com.simprints.id.orchestrator.modality.steps.Step.Status.ONGOING
import com.simprints.id.orchestrator.modality.steps.StepProcessor
import com.simprints.moduleapi.face.responses.IFaceResponse
import com.simprints.moduleapi.face.responses.IFaceResponse.Companion.BUNDLE_KEY

interface FingerprintEnrolStepProcessor : StepProcessor {
    fun buildStep(enrolAppRequest: AppEnrolRequest): Step
}

class FingerprintEnrolStepProcessorImpl(private val fingerprintRequestFactory: FingerprintRequestFactory,
                                        private val prefs: PreferencesManager,
                                        private val packageName: String) : BaseFingerprintStepProcessor(), FingerprintEnrolStepProcessor {

    override val requestCode = FINGERPRINT_REQUEST_CODE + 1

    override fun buildStep(enrolAppRequest: AppEnrolRequest): Step {
        val fingerprintRequest = fingerprintRequestFactory.buildFingerprintRequest(enrolAppRequest, prefs)
        val intent = buildIntent(DomainToFingerprintRequest.fromDomainToFingerprintRequest(fingerprintRequest), packageName)
        return Step(intent, ONGOING)
    }

    override fun processResult(requestCode: Int, resultCode: Int, data: Intent?): FaceResponse? =
        data?.getParcelableExtra<IFaceResponse>(BUNDLE_KEY)?.let {
            FaceToDomainResponse.fromFaceToDomainResponse(it)
        }
}
