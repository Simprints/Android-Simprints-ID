package com.simprints.id.orchestrator.steps.face

import android.content.Intent
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.requests.FaceRequest
import com.simprints.id.domain.moduleapi.face.responses.fromModuleApiToDomain
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.*
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.Companion.isFaceResult
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse

class FaceStepProcessorImpl(private val faceRequestFactory: FaceRequestFactory) : FaceStepProcessor {

    companion object {
        const val ACTIVITY_CLASS_NAME = "com.simprints.face.activities.FaceCaptureActivity"
        const val N_FACE_SAMPLES_TO_CAPTURE: Int = 1
    }

    override fun buildCaptureStep(): Step =
        faceRequestFactory.buildCaptureRequest(N_FACE_SAMPLES_TO_CAPTURE).run {
            buildStep(CAPTURE, this)
        }

    override fun buildStepIdentify(projectId: String,
                                   userId: String,
                                   moduleId: String): Step =
        faceRequestFactory.buildFaceIdentifyRequest(projectId, userId, moduleId).run {
            buildStep(IDENTIFY, this)
        }

    override fun buildStepVerify(projectId: String,
                                 userId: String,
                                 moduleId: String): Step =
        faceRequestFactory.buildFaceVerifyRequest(projectId, userId, moduleId).run {
            buildStep(VERIFY, this)
        }

    private fun buildStep(requestCode: FaceRequestCode, request: FaceRequest): Step =
        Step(requestCode.value, ACTIVITY_CLASS_NAME, IFaceRequest.BUNDLE_KEY, request, status = Step.Status.NOT_STARTED)

    override fun processResult(requestCode: Int, resultCode: Int, data: Intent?): Step.Result? =
        if (isFaceResult(requestCode)) {
            data?.getParcelableExtra<IFaceResponse>(IFaceResponse.BUNDLE_KEY)?.fromModuleApiToDomain()
        } else {
            null
        }
}
