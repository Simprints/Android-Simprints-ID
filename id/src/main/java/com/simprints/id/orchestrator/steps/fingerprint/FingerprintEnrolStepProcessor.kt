package com.simprints.id.orchestrator.steps.fingerprint

import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.fingerprint.DomainToFingerprintRequest.fromDomainToFingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.StepProcessor

interface FingerprintEnrolStepProcessor : StepProcessor {
    fun buildStep(enrolAppRequest: AppEnrolRequest): Step
}

class FingerprintEnrolStepProcessorImpl(private val fingerprintRequestFactory: FingerprintRequestFactory,
                                        private val prefs: PreferencesManager,
                                        private val packageName: String) : BaseFingerprintStepProcessor(), FingerprintEnrolStepProcessor {

    override val requestCode = FINGERPRINT_ENROL_REQUEST_CODE

    override fun buildStep(enrolAppRequest: AppEnrolRequest): Step {
        val fingerprintRequest = fingerprintRequestFactory.buildFingerprintRequest(enrolAppRequest, prefs)
        val intent = buildIntent(fromDomainToFingerprintRequest(fingerprintRequest), packageName)
        return Step(intent, NOT_STARTED)
    }
}
