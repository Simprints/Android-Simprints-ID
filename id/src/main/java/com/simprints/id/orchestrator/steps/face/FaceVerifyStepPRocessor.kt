package com.simprints.id.orchestrator.steps.face

import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.requests.DomainToFaceRequest.fromDomainToFaceRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.StepProcessor

interface FaceVerifyStepProcessor : StepProcessor {
    fun buildStep(verifyRequest: AppVerifyRequest): Step
}

class FaceVerifyStepProcessorImpl(private val faceRequestFactory: FaceRequestFactory,
                                  private val packageName: String) : BaseFaceStepProcessor(), FaceVerifyStepProcessor {

    override val requestCode = FACE_REQUEST_CODE + 2

    override fun buildStep(verifyRequest: AppVerifyRequest): Step {
        val faceRequest = faceRequestFactory.buildFaceRequest(verifyRequest)
        val intent = buildIntent(fromDomainToFaceRequest(faceRequest), packageName)
        return Step(intent, NOT_STARTED)
    }
}
