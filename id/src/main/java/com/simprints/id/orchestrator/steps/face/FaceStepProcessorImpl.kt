package com.simprints.id.orchestrator.steps.face

import android.content.Intent
import com.simprints.id.data.db.person.domain.FaceSample
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.requests.FaceRequest
import com.simprints.id.domain.moduleapi.face.responses.fromModuleApiToDomain
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.CAPTURE
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.Companion.isFaceResult
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.IDENTIFY
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse

class FaceStepProcessorImpl(private val faceRequestFactory: FaceRequestFactory) : FaceStepProcessor {

    companion object {
        const val ACTIVITY_CLASS_NAME = "com.simprints.face.activities.orchestrator.FaceOrchestratorActivity"
        const val N_FACE_SAMPLES_TO_CAPTURE: Int = 1
    }

    override fun buildCaptureStep(): Step =
        faceRequestFactory.buildCaptureRequest(N_FACE_SAMPLES_TO_CAPTURE).run {
            buildStep(CAPTURE, this)
        }

    override fun buildStepMatch(probeFaceSample: List<FaceSample>, query: PersonLocalDataSource.Query): Step =
        faceRequestFactory.buildFaceMatchRequest(probeFaceSample, query).run {
            buildStep(IDENTIFY, this)
        }

    private fun buildStep(requestCode: FaceRequestCode, request: FaceRequest): Step {
        return Step(
            requestCode = requestCode.value,
            activityName = ACTIVITY_CLASS_NAME,
            bundleKey = IFaceRequest.BUNDLE_KEY,
            request = request,
            status = Step.Status.NOT_STARTED
        )
    }

    override fun processResult(requestCode: Int, resultCode: Int, data: Intent?): Step.Result? =
        if (isFaceResult(requestCode)) {
            data?.getParcelableExtra<IFaceResponse>(IFaceResponse.BUNDLE_KEY)?.fromModuleApiToDomain()
        } else {
            null
        }
}
