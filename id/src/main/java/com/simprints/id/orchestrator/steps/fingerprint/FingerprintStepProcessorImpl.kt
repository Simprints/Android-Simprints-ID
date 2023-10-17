package com.simprints.id.orchestrator.steps.fingerprint

import android.content.Intent
import android.os.Parcelable
import com.simprints.core.domain.common.FlowProvider
import com.simprints.matcher.FingerprintMatchResult
import com.simprints.matcher.MatchContract
import com.simprints.matcher.MatchParams
import com.simprints.matcher.screen.MatchWrapperActivity
import com.simprints.id.domain.moduleapi.fingerprint.models.fromDomainToModuleApi
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
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
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintMatchResult as FingerprintMatchResponseResult
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse.Companion.BUNDLE_KEY as RESPONSE_BUNDLE_KEY

class FingerprintStepProcessorImpl @Inject constructor(
    private val configManager: ConfigManager,
) : FingerprintStepProcessor {

    companion object {
        const val MATCHER_ACTIVITY_NAME = "com.simprints.matcher.screen.MatchWrapperActivity"
        const val CAPTURE_ACTIVITY_NAME = "com.simprints.fingerprint.activities.orchestrator.OrchestratorActivity"
    }

    override suspend fun buildStepToCapture(): Step = Step(
        requestCode = CAPTURE.value,
        activityName = CAPTURE_ACTIVITY_NAME,
        bundleKey = IFingerprintRequest.BUNDLE_KEY,
        payloadType = Step.PayloadType.REQUEST,
        payload = FingerprintCaptureRequest(fingerprintsToCapture = configManager.getProjectConfiguration().fingerprint!!.fingersToCapture),
        status = NOT_STARTED
    )

    override fun buildStepToMatch(
        probeSamples: List<FingerprintCaptureSample>,
        query: SubjectQuery,
        flowType: FlowProvider.FlowType,
    ): Step = Step(
        requestCode = MATCH.value,
        activityName = MATCHER_ACTIVITY_NAME,
        bundleKey = MatchWrapperActivity.MATCHER_ARGS_EXTRA,
        payloadType = Step.PayloadType.BUNDLE,
        payload = MatchContract.getArgs(
            fingerprintSamples = probeSamples.map { sample ->
                MatchParams.FingerprintSample(
                    sample.fingerIdentifier.fromDomainToModuleApi(),
                    sample.format,
                    sample.template,
                )
            },
            flowType = flowType,
            subjectQuery = query,
        ),
        status = NOT_STARTED
    )

    override fun processResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): FingerprintResponse? {
        if (!isFingerprintResult(requestCode)) {
            return null
        }

        if (data?.extras?.containsKey(MatchContract.RESULT) == true) {
            return when (val result = data.getParcelableExtra<Parcelable>(MatchContract.RESULT)) {
                is FingerprintMatchResult -> FingerprintMatchResponse(result.results.map { FingerprintMatchResponseResult(it.subjectId, it.confidence) })
                else -> null
            }
        }

        // TODO handle capture result after refactoring
        return data?.getParcelableExtra<IFingerprintResponse>(RESPONSE_BUNDLE_KEY)?.fromModuleApiToDomain()
    }

}
