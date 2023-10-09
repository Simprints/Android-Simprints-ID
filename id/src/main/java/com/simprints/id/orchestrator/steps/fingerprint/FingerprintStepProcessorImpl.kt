package com.simprints.id.orchestrator.steps.fingerprint

import android.content.Intent
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintMatchRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureSample
import com.simprints.id.domain.moduleapi.fingerprint.responses.fromModuleApiToDomain
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode.CAPTURE
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode.Companion.isFingerprintResult
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode.MATCH
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse
import javax.inject.Inject
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse.Companion.BUNDLE_KEY as RESPONSE_BUNDLE_KEY

class FingerprintStepProcessorImpl @Inject constructor(
    private val configManager: ConfigManager,
) : FingerprintStepProcessor {

    companion object {
        const val ACTIVITY_CLASS_NAME =
            "com.simprints.fingerprint.activities.orchestrator.OrchestratorActivity"
    }

    override suspend fun buildStepToCapture(): Step = buildStep(
        CAPTURE,
        FingerprintCaptureRequest(fingerprintsToCapture = configManager.getProjectConfiguration().fingerprint!!.fingersToCapture)
    )

    override fun buildStepToMatch(
        probeSamples: List<FingerprintCaptureSample>,
        query: SubjectQuery
    ): Step = buildStep(MATCH, FingerprintMatchRequest(probeSamples, query))

    private fun buildStep(requestCode: FingerprintRequestCode, request: FingerprintRequest): Step {
        return Step(
            requestCode = requestCode.value,
            activityName = ACTIVITY_CLASS_NAME,
            bundleKey = IFingerprintRequest.BUNDLE_KEY,
            payloadType = Step.PayloadType.REQUEST,
            payload = request,
            status = NOT_STARTED
        )
    }

    override fun processResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): FingerprintResponse? =
        if (isFingerprintResult(requestCode)) {
            data?.getParcelableExtra<IFingerprintResponse>(RESPONSE_BUNDLE_KEY)?.fromModuleApiToDomain()
        } else {
            null
        }

}
