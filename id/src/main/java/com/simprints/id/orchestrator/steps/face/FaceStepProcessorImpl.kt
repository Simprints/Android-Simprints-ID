package com.simprints.id.orchestrator.steps.face

import android.content.Intent
import android.os.Parcelable
import com.simprints.core.domain.common.FlowProvider
import com.simprints.face.capture.FaceCaptureContract
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.face.capture.screens.FaceCaptureWrapperActivity
import com.simprints.face.configuration.FaceConfigurationContract
import com.simprints.face.configuration.FaceConfigurationResult
import com.simprints.face.configuration.screen.FaceConfigurationWrapperActivity
import com.simprints.matcher.MatchContract
import com.simprints.matcher.MatchParams
import com.simprints.matcher.FaceMatchResult
import com.simprints.matcher.screen.MatchWrapperActivity
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceConfigurationResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceExitFormResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceExitReason
import com.simprints.id.domain.moduleapi.face.responses.FaceMatchResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.domain.moduleapi.face.responses.entities.fromModuleApiToDomain
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.CAPTURE
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.CONFIGURATION
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.Companion.isFaceResult
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.MATCH
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import javax.inject.Inject
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceMatchResult as FaceMatchResponseResult

class FaceStepProcessorImpl @Inject constructor(
    private val configManager: ConfigManager,
) : FaceStepProcessor {

    companion object {
        const val CONFIGURATION_ACTIVITY_NAME = "com.simprints.face.configuration.screen.FaceConfigurationWrapperActivity"
        const val MATCHER_ACTIVITY_NAME = "com.simprints.matcher.screen.MatchWrapperActivity"
        const val CAPTURE_ACTIVITY_NAME = "com.simprints.face.capture.screens.FaceCaptureWrapperActivity"
    }

    override suspend fun buildCaptureStep(): Step = Step(
        requestCode = CAPTURE.value,
        activityName = CAPTURE_ACTIVITY_NAME,
        bundleKey = FaceCaptureWrapperActivity.FACE_CAPTURE_ARGS_EXTRA,
        payloadType = Step.PayloadType.BUNDLE,
        payload = FaceCaptureContract.getArgs(
            configManager.getProjectConfiguration().face?.nbOfImagesToCapture ?: 0
        ),
        status = Step.Status.NOT_STARTED
    )

    override fun buildStepMatch(
        probeFaceSample: List<FaceCaptureSample>,
        query: SubjectQuery,
        flowType: FlowProvider.FlowType,
    ): Step = Step(
        requestCode = MATCH.value,
        activityName = MATCHER_ACTIVITY_NAME,
        bundleKey = MatchWrapperActivity.MATCHER_ARGS_EXTRA,
        payloadType = Step.PayloadType.BUNDLE,
        payload = MatchContract.getArgs(
            faceSamples = probeFaceSample.map { MatchParams.FaceSample(it.faceId, it.template) },
            flowType = flowType,
            subjectQuery = query,
        ),
        status = Step.Status.NOT_STARTED
    )

    override fun buildConfigurationStep(projectId: String, deviceId: String): Step = Step(
        requestCode = CONFIGURATION.value,
        activityName = CONFIGURATION_ACTIVITY_NAME,
        bundleKey = FaceConfigurationWrapperActivity.FACE_CONFIGURATION_ARGS_EXTRA,
        payloadType = Step.PayloadType.BUNDLE,
        payload = FaceConfigurationContract.getArgs(projectId, deviceId),
        status = Step.Status.NOT_STARTED
    )

    override fun processResult(requestCode: Int, resultCode: Int, data: Intent?): Step.Result? {
        if (!isFaceResult(requestCode)) {
            return null
        }

        if (data?.extras?.containsKey(FaceCaptureContract.RESULT) == true) {
            val res = when (val result = data.getParcelableExtra<Parcelable>(FaceCaptureContract.RESULT)) {
                is FaceCaptureResult -> FaceCaptureResponse(result.results.map { it.fromModuleApiToDomain() })
                is ExitFormResult -> result.submittedOption()
                    ?.let { FaceExitFormResponse(FaceExitReason.fromExitFormOption(it), result.reason.orEmpty()) }

                else -> null
            }
            return res
        }

        if (data?.extras?.containsKey(MatchContract.RESULT) == true) {
            return when (val result = data.getParcelableExtra<Parcelable>(MatchContract.RESULT)) {
                is FaceMatchResult -> FaceMatchResponse(result.results.map { FaceMatchResponseResult(it.guid, it.confidence) })
                else -> null
            }
        }

        if (data?.extras?.containsKey(FaceConfigurationContract.RESULT) == true) {
            return when (data.getParcelableExtra<Parcelable>(FaceConfigurationContract.RESULT)) {
                is FaceConfigurationResult -> FaceConfigurationResponse()
                else -> null
            }
        }
        return null
    }

}
