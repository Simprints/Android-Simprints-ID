package com.simprints.id.orchestrator.steps.face

import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.requests.DomainToFaceRequest.fromDomainToFaceRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.StepProcessor

interface FaceIdentifyStepProcessor : StepProcessor {
    fun buildStep(identifyRequest: AppIdentifyRequest): Step
}

class FaceIdentifyStepProcessorImpl(private val faceRequestFactory: FaceRequestFactory,
                                    private val packageName: String) : BaseFaceStepProcessor(), FaceIdentifyStepProcessor {

    override val requestCode = FACE_IDENTIFY_REQUEST_CODE

    override fun buildStep(identifyRequest: AppIdentifyRequest): Step {
        val faceRequest = faceRequestFactory.buildFaceRequest(identifyRequest)
        val intent = buildIntent(fromDomainToFaceRequest(faceRequest), packageName)
        return Step(intent, NOT_STARTED)
    }
}
