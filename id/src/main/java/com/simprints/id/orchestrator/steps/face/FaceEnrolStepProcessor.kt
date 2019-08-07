package com.simprints.id.orchestrator.steps.face

import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.requests.DomainToFaceRequest.fromDomainToFaceRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.StepProcessor

interface FaceEnrolStepProcessor : StepProcessor {
    fun buildStep(enrolRequest: AppEnrolRequest): Step
}

class FaceEnrolStepProcessorImpl(private val faceRequestFactory: FaceRequestFactory,
                                 private val packageName: String) : BaseFaceStepProcessor(), FaceEnrolStepProcessor {

    override val requestCode = FACE_ENROL_REQUEST_CODE

    override fun buildStep(enrolRequest: AppEnrolRequest): Step {
        val faceRequest = faceRequestFactory.buildFaceRequest(enrolRequest)
        val intent = buildIntent(fromDomainToFaceRequest(faceRequest), packageName)
        return Step(intent, NOT_STARTED)
    }
}
