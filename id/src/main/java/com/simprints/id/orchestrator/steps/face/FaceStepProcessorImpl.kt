package com.simprints.id.orchestrator.steps.face

import android.content.Intent
import android.os.Parcelable
import com.simprints.face.configuration.FaceConfigurationContract
import com.simprints.face.configuration.FaceConfigurationResult
import com.simprints.face.configuration.screen.FaceConfigurationWrapperActivity
import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceMatchRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceConfigurationResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.domain.moduleapi.face.responses.fromModuleApiToDomain
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.CAPTURE
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.CONFIGURATION
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.Companion.isFaceResult
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.MATCH
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import javax.inject.Inject

class FaceStepProcessorImpl @Inject constructor(
    private val configManager: ConfigManager,
) : FaceStepProcessor {

    companion object {
        const val ACTIVITY_CLASS_NAME = "com.simprints.face.orchestrator.FaceOrchestratorActivity"
        const val CONFIGURATION_CLASS_NAME = "com.simprints.face.configuration.screen.FaceConfigurationWrapperActivity"
    }

    override suspend fun buildCaptureStep(): Step = buildStep(
        CAPTURE,
        FaceCaptureRequest(configManager.getProjectConfiguration().face?.nbOfImagesToCapture ?: 0)
    )

    override fun buildStepMatch(
        probeFaceSample: List<FaceCaptureSample>,
        query: SubjectQuery
    ): Step = buildStep(
        MATCH,
        FaceMatchRequest(probeFaceSample, query)
    )

    private fun buildStep(requestCode: FaceRequestCode, request: FaceRequest) = Step(
        requestCode = requestCode.value,
        activityName = ACTIVITY_CLASS_NAME,
        bundleKey = IFaceRequest.BUNDLE_KEY,
        payloadType = Step.PayloadType.REQUEST,
        payload = request,
        status = Step.Status.NOT_STARTED
    )

    override fun buildConfigurationStep(projectId: String, deviceId: String): Step = Step(
        requestCode = CONFIGURATION.value,
        activityName = CONFIGURATION_CLASS_NAME,
        bundleKey = FaceConfigurationWrapperActivity.FACE_CONFIGURATION_ARGS_EXTRA,
        payloadType = Step.PayloadType.BUNDLE,
        payload = FaceConfigurationContract.getArgs(projectId, deviceId),
        status = Step.Status.NOT_STARTED
    )

    override fun processResult(requestCode: Int, resultCode: Int, data: Intent?): Step.Result? {
        if (!isFaceResult(requestCode)) {
            return null
        }

        val legacyFaceResult = data?.getParcelableExtra<IFaceResponse>(IFaceResponse.BUNDLE_KEY)
        if (legacyFaceResult != null) {
            return legacyFaceResult.fromModuleApiToDomain()
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
