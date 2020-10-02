package com.simprints.id.orchestrator.steps.face

import android.content.Intent
import com.simprints.id.data.db.subject.local.SubjectQuery
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.requests.FaceRequest
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.domain.moduleapi.face.responses.fromModuleApiToDomain
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.*
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.Companion.isFaceResult
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse

class FaceStepProcessorImpl(
    private val faceRequestFactory: FaceRequestFactory,
    private val prefs: PreferencesManager
) : FaceStepProcessor {

    companion object {
        const val ACTIVITY_CLASS_NAME = "com.simprints.face.orchestrator.FaceOrchestratorActivity"
    }

    override fun buildCaptureStep(): Step =
        faceRequestFactory.buildCaptureRequest(prefs.faceNbOfFramesCaptured).run {
            buildStep(CAPTURE, this)
        }

    override fun buildStepMatch(probeFaceSample: List<FaceCaptureSample>, query: SubjectQuery): Step =
        faceRequestFactory.buildFaceMatchRequest(probeFaceSample, query).run {
            buildStep(MATCH, this)
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
            data?.getParcelableExtra<IFaceResponse>(IFaceResponse.BUNDLE_KEY)
                ?.fromModuleApiToDomain()
        } else {
            null
        }

    override fun buildConfigurationStep(projectId: String, deviceId: String): Step =
        faceRequestFactory.buildFaceConfigurationRequest(projectId, deviceId).run {
            buildStep(CONFIGURATION, this)
        }

}
