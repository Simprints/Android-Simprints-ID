package com.simprints.id.orchestrator.steps.face

import android.content.Intent
import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceConfigurationRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceMatchRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceRequest
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

    override fun buildConfigurationStep(projectId: String, deviceId: String): Step = buildStep(
        CONFIGURATION,
        FaceConfigurationRequest(projectId, deviceId)
    )

    private fun buildStep(requestCode: FaceRequestCode, request: FaceRequest) = Step(
        requestCode = requestCode.value,
        activityName = ACTIVITY_CLASS_NAME,
        bundleKey = IFaceRequest.BUNDLE_KEY,
        payloadType = Step.PayloadType.REQUEST,
        payload = request,
        status = Step.Status.NOT_STARTED
    )

    override fun processResult(requestCode: Int, resultCode: Int, data: Intent?): Step.Result? =
        if (isFaceResult(requestCode)) {
            data?.getParcelableExtra<IFaceResponse>(IFaceResponse.BUNDLE_KEY)?.fromModuleApiToDomain()
        } else {
            null
        }

}
