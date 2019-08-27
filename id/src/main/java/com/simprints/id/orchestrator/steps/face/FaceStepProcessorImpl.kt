package com.simprints.id.orchestrator.steps.face
import android.content.Intent
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.ModuleApiToDomainFaceResponse
import com.simprints.id.domain.moduleapi.face.requests.FaceRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.Companion.isFaceResult
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.*

import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse

class FaceStepProcessorImpl(private val faceRequestFactory: FaceRequestFactory,
                            private val converterModuleApiToDomain: ModuleApiToDomainFaceResponse) : FaceStepProcessor {

    companion object {
        const val ACTIVITY_CLASS_NAME = "com.simprints.face.activities.FaceCaptureActivity"
    }

    override fun buildStepEnrol(projectId: String,
                                userId: String,
                                moduleId: String): Step =
        faceRequestFactory.buildFaceEnrolRequest(projectId, userId, moduleId).run {
            buildStep(ENROL, this)
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
        Step(requestCode.value, ACTIVITY_CLASS_NAME, IFaceRequest.BUNDLE_KEY, request, Step.Status.NOT_STARTED)

    override fun processResult(requestCode: Int, resultCode: Int, data: Intent?): Step.Result? =
        if (isFaceResult(requestCode)) {
            data?.getParcelableExtra<IFaceResponse>(IFaceResponse.BUNDLE_KEY)?.let {
                converterModuleApiToDomain.fromModuleApiToDomainFaceResponse(it)
            }
        } else {
            null
        }
}
