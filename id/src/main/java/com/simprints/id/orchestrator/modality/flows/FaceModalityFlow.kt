package com.simprints.id.orchestrator.modality.flows

import android.app.Activity
import android.content.Intent
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.FaceToDomainResponse.fromFaceToDomainResponse
import com.simprints.id.domain.moduleapi.face.requests.DomainToFaceRequest.fromDomainToFaceRequest
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow.Request
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow.Step
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow.Step.Status.ONGOING
import com.simprints.id.orchestrator.modality.flows.interfaces.SingleModalityFlow
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse

class FaceModalityFlow(private val appRequest: AppRequest,
                       private val packageName: String,
                       private val faceRequestFactory: FaceRequestFactory) : SingleModalityFlow {

    companion object {
        const val faceActivityClassName = "com.simprints.face.activities.FaceCaptureActivity"
        const val REQUEST_CODE_FACE = 1
    }

    override val steps by lazy {
        listOf(Step(getModalityStepRequestForFace(), ONGOING))
    }

    override fun getLatestOngoingStep(): Step? = steps.firstOrNull { it.status == ONGOING }

    internal fun getModalityStepRequestForFace(): Request {
        val intent = Intent().setClassName(packageName, faceActivityClassName)
        val domainFaceRequest = faceRequestFactory.buildFaceRequest(appRequest)
        intent.putExtra(IFaceRequest.BUNDLE_KEY, fromDomainToFaceRequest(domainFaceRequest))
        return Request(REQUEST_CODE_FACE, intent)
    }

    @Throws(IllegalArgumentException::class)
    override fun handleIntentResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean =
        isFaceResult(requestCode).also { isFace ->
            if (isFace) {
                require(resultCode == Activity.RESULT_OK && data != null)
                processResult(data)
            }
        }

    private fun processResult(data: Intent) {
        val potentialFaceResponse = data.getParcelableExtra<IFaceResponse>(IFaceResponse.BUNDLE_KEY)
        steps[0].result = fromFaceToDomainResponse(potentialFaceResponse)
    }

    private fun isFaceResult(requestCode: Int): Boolean = requestCode == REQUEST_CODE_FACE
}
