package com.simprints.id.orchestrator.steps.fingerprint

import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.fingerprint.DomainToFingerprintRequest.fromDomainToFingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.StepProcessor

interface FingerprintVerifyStepProcessor : StepProcessor {
    fun buildStep(verifyRequest: AppVerifyRequest): Step
}

class FingerprintVerifyStepProcessorImpl(private val fingerprintRequestFactory: FingerprintRequestFactory,
                                         private val prefs: PreferencesManager,
                                         private val packageName: String) : BaseFingerprintStepProcessor(), FingerprintVerifyStepProcessor {

    override val requestCode = FINGERPRINT_REQUEST_CODE + 3

    override fun buildStep(verifyRequest: AppVerifyRequest): Step {
        val fingerprintRequest = fingerprintRequestFactory.buildFingerprintRequest(verifyRequest, prefs)
        val intent = buildIntent(fromDomainToFingerprintRequest(fingerprintRequest), packageName)
        return Step(intent, NOT_STARTED)
    }
}
