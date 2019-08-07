package com.simprints.id.orchestrator.modality.steps.face

import android.content.Intent
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.FaceToDomainResponse
import com.simprints.id.domain.moduleapi.face.requests.DomainToFaceRequest
import com.simprints.id.orchestrator.modality.steps.Step
import com.simprints.id.orchestrator.modality.steps.Step.Result
import com.simprints.id.orchestrator.modality.steps.Step.Status.ONGOING
import com.simprints.id.orchestrator.modality.steps.StepProcessor
import com.simprints.moduleapi.face.responses.IFaceResponse
import com.simprints.moduleapi.face.responses.IFaceResponse.Companion.BUNDLE_KEY

interface FaceIdentifyStepProcessor : StepProcessor {
    fun buildStep(identifyRequest: AppIdentifyRequest): Step
}

class FaceIdentifyStepProcessorImpl(private val faceRequestFactory: FaceRequestFactory,
                                    private val packageName: String) : BaseFaceStepProcessor(), FaceIdentifyStepProcessor {

    override val requestCode = FACE_REQUEST_CODE + 3

    override fun buildStep(identifyRequest: AppIdentifyRequest): Step {
        val faceRequest = faceRequestFactory.buildFaceRequest(identifyRequest)
        val intent = buildIntent(DomainToFaceRequest.fromDomainToFaceRequest(faceRequest), packageName)
        return Step(intent, ONGOING)
    }

    override fun processResult(requestCode: Int, resultCode: Int, data: Intent?): Result? =
        data?.getParcelableExtra<IFaceResponse>(BUNDLE_KEY)?.let {
            FaceToDomainResponse.fromFaceToDomainResponse(it)
        }
}
