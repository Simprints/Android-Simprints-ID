package com.simprints.id.orchestrator.steps.fingerprint

import android.content.Intent
import com.simprints.id.data.db.subject.local.SubjectQuery
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureSample
import com.simprints.id.domain.moduleapi.fingerprint.responses.fromModuleApiToDomain
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode.*
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode.Companion.isFingerprintResult
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse.Companion.BUNDLE_KEY as RESPONSE_BUNDLE_KEY

class FingerprintStepProcessorImpl(private val fingerprintRequestFactory: FingerprintRequestFactory,
                                   private val prefs: IdPreferencesManager
) : FingerprintStepProcessor {

    companion object {
        const val ACTIVITY_CLASS_NAME = "com.simprints.fingerprint.activities.orchestrator.OrchestratorActivity"
    }

    override fun buildStepToCapture(): Step =
        fingerprintRequestFactory.buildFingerprintCaptureRequest(prefs).run {
            buildStep(CAPTURE, this)
        }

    override fun buildStepToMatch(probeSamples: List<FingerprintCaptureSample>, query: SubjectQuery): Step =
        fingerprintRequestFactory.buildFingerprintMatchRequest(probeSamples, query).run {
            buildStep(MATCH, this)
        }

    private fun buildStep(requestCode: FingerprintRequestCode, request: FingerprintRequest): Step {
        return Step(
            requestCode = requestCode.value,
            activityName = ACTIVITY_CLASS_NAME,
            bundleKey = IFingerprintRequest.BUNDLE_KEY,
            request = request,
            status = NOT_STARTED
        )
    }

    override fun processResult(requestCode: Int, resultCode: Int, data: Intent?): FingerprintResponse? =
        if (isFingerprintResult(requestCode)) {
            data?.getParcelableExtra<IFingerprintResponse>(RESPONSE_BUNDLE_KEY)?.fromModuleApiToDomain()
        } else {
            null
        }

    override fun buildConfigurationStep(): Step =
        fingerprintRequestFactory.buildFingerprintConfigurationRequest().run {
            buildStep(CONFIGURATION, this)
        }

}
