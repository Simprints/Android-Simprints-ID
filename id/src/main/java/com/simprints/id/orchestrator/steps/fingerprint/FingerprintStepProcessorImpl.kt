package com.simprints.id.orchestrator.steps.fingerprint

import android.content.Intent
import android.os.Parcelable
import com.simprints.core.domain.common.FlowProvider
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.fingerprint.capture.FingerprintCaptureContract
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.fingerprint.capture.screen.FingerprintCaptureWrapperActivity
import com.simprints.id.domain.moduleapi.face.responses.FaceExitFormResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceExitReason
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureSample
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.fromModuleApiToDomain
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode.CAPTURE
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode.Companion.isFingerprintResult
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode.MATCH
import com.simprints.infra.config.store.models.fromDomainToModuleApi
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.matcher.FingerprintMatchResult
import com.simprints.matcher.MatchContract
import com.simprints.matcher.MatchParams
import com.simprints.matcher.screen.MatchWrapperActivity
import javax.inject.Inject
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintMatchResult as FingerprintMatchResponseResult

class FingerprintStepProcessorImpl @Inject constructor(
    private val configManager: ConfigManager,
) : FingerprintStepProcessor {

    companion object {
        const val MATCHER_ACTIVITY_NAME = "com.simprints.matcher.screen.MatchWrapperActivity"
        const val CAPTURE_ACTIVITY_NAME = "com.simprints.fingerprint.capture.screen.FingerprintCaptureWrapperActivity"
    }

    override suspend fun buildStepToCapture(flowType: FlowProvider.FlowType): Step = Step(
        requestCode = CAPTURE.value,
        activityName = CAPTURE_ACTIVITY_NAME,
        bundleKey = FingerprintCaptureWrapperActivity.FINGERPRINT_CAPTURE_ARGS_EXTRA,
        payloadType = Step.PayloadType.BUNDLE,
        payload = FingerprintCaptureContract.getArgs(
            flowType = flowType,
            fingers = configManager.getProjectConfiguration()
                .fingerprint
                ?.fingersToCapture.orEmpty()
                .map { it.fromDomainToModuleApi() },
        ),
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
    ): Step.Result? {
        if (!isFingerprintResult(requestCode)) {
            return null
        }

        if (data?.extras?.containsKey(FingerprintCaptureContract.RESULT) == true) {
            val res = when (val result = data.getParcelableExtra<Parcelable>(FingerprintCaptureContract.RESULT)) {
                is FingerprintCaptureResult -> FingerprintCaptureResponse(result.results.map { it.fromModuleApiToDomain() })
                is ExitFormResult -> result.submittedOption()
                    ?.let { FaceExitFormResponse(FaceExitReason.fromExitFormOption(it), result.reason.orEmpty()) }

                else -> null
            }
            return res
        }
        if (data?.extras?.containsKey(MatchContract.RESULT) == true) {
            return when (val result = data.getParcelableExtra<Parcelable>(MatchContract.RESULT)) {
                is FingerprintMatchResult -> FingerprintMatchResponse(result.results.map { FingerprintMatchResponseResult(it.subjectId, it.confidence) })
                else -> null
            }
        }
        return null
    }

}
