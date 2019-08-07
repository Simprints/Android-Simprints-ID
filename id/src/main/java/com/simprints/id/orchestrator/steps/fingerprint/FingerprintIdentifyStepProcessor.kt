package com.simprints.id.orchestrator.steps.fingerprint

import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.fingerprint.DomainToFingerprintRequest.fromDomainToFingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.StepProcessor

interface FingerprintIdentifyStepProcessor : StepProcessor {
    fun buildStep(identifyRequest: AppIdentifyRequest): Step
}

class FingerprintIdentifyStepProcessorImpl(private val fingerprintRequestFactory: FingerprintRequestFactory,
                                           private val prefs: PreferencesManager,
                                           private val packageName: String) : BaseFingerprintStepProcessor(), FingerprintIdentifyStepProcessor {

    override val requestCode = FINGERPRINT_IDENTIFY_REQUEST_CODE

    override fun buildStep(identifyRequest: AppIdentifyRequest): Step {
        val fingerprintRequest = fingerprintRequestFactory.buildFingerprintRequest(identifyRequest, prefs)
        val intent = buildIntent(fromDomainToFingerprintRequest(fingerprintRequest), packageName)
        return Step(intent, NOT_STARTED)
    }
}
