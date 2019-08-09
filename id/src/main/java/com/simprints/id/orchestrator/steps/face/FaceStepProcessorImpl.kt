package com.simprints.id.orchestrator.steps.face
import android.content.Intent
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.ModuleApiToDomainFaceResponse
import com.simprints.id.domain.moduleapi.face.requests.FaceRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse

class FaceStepProcessorImpl(private val faceRequestFactory: FaceRequestFactory,
                            private val converterModuleApiToDomain: ModuleApiToDomainFaceResponse) : FaceStepProcessor {

    companion object {
        const val ACTIVITY_CLASS_NAME = "com.simprints.face.activities.FaceCaptureActivity"

        private const val FACE_REQUEST_CODE = 200
        const val FACE_ENROL_REQUEST_CODE = FACE_REQUEST_CODE + 1
        const val FACE_IDENTIFY_REQUEST_CODE = FACE_REQUEST_CODE + 2
        const val FACE_VERIFY_REQUEST_CODE = FACE_REQUEST_CODE + 3

        fun isFaceResult(requestCode: Int) =
            listOf(FACE_ENROL_REQUEST_CODE, FACE_IDENTIFY_REQUEST_CODE, FACE_VERIFY_REQUEST_CODE).contains(requestCode)
    }

    override fun buildStepEnrol(projectId: String,
                                userId: String,
                                moduleId: String): Step =
        faceRequestFactory.buildFaceEnrolRequest(projectId, userId, moduleId).run {
            buildStep(FACE_ENROL_REQUEST_CODE, this)
        }

    override fun buildStepIdentify(projectId: String,
                                   userId: String,
                                   moduleId: String): Step =
        faceRequestFactory.buildFaceIdentifyRequest(projectId, userId, moduleId).run {
            buildStep(FACE_IDENTIFY_REQUEST_CODE, this)
        }

    override fun buildStepVerify(projectId: String,
                                 userId: String,
                                 moduleId: String): Step =
        faceRequestFactory.buildFaceVerifyRequest(projectId, userId, moduleId).run {
            buildStep(FACE_VERIFY_REQUEST_CODE, this)
        }

    private fun buildStep(requestCode: Int, request: FaceRequest): Step =
        Step(requestCode, ACTIVITY_CLASS_NAME, IFaceRequest.BUNDLE_KEY, request, Step.Status.NOT_STARTED)

    override fun processResult(requestCode: Int, resultCode: Int, data: Intent?): Step.Result? =
        if (isFaceResult(requestCode)) {
            data?.getParcelableExtra<IFaceResponse>(IFaceResponse.BUNDLE_KEY)?.let {
                converterModuleApiToDomain.fromModuleApiToDomainFaceResponse(it)
            }
        } else {
            null
        }
}
