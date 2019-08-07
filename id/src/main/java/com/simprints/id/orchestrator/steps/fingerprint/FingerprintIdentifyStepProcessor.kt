package com.simprints.id.orchestrator.modality.steps.fingerprint

import android.content.Intent
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.face.FaceToDomainResponse
import com.simprints.id.domain.moduleapi.fingerprint.DomainToFingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory
import com.simprints.id.orchestrator.modality.steps.Step
import com.simprints.id.orchestrator.modality.steps.Step.Result
import com.simprints.id.orchestrator.modality.steps.Step.Status.ONGOING
import com.simprints.id.orchestrator.modality.steps.StepProcessor
import com.simprints.moduleapi.face.responses.IFaceResponse
import com.simprints.moduleapi.face.responses.IFaceResponse.Companion.BUNDLE_KEY

interface FingerprintIdentifyStepProcessor : StepProcessor {
    fun buildStep(identifyRequest: AppIdentifyRequest): Step
}

class FingerprintIdentifyStepProcessorImpl(private val fingerprintRequestFactory: FingerprintRequestFactory,
                                           private val prefs: PreferencesManager,
                                           private val packageName: String) : BaseFingerprintStepProcessor(), FingerprintIdentifyStepProcessor {

    override val requestCode = FINGERPRINT_REQUEST_CODE + 2

    override fun buildStep(identifyRequest: AppIdentifyRequest): Step {
        val fingerprintRequest = fingerprintRequestFactory.buildFingerprintRequest(identifyRequest, prefs)
        val intent = buildIntent(DomainToFingerprintRequest.fromDomainToFingerprintRequest(fingerprintRequest), packageName)
        return Step(intent, ONGOING)
    }

    override fun processResult(requestCode: Int, resultCode: Int, data: Intent?): Result? =
        data?.getParcelableExtra<IFaceResponse>(BUNDLE_KEY)?.let {
            FaceToDomainResponse.fromFaceToDomainResponse(it)
        }
}
