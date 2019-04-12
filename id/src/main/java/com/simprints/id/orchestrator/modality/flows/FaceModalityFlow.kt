package com.simprints.id.orchestrator.modality.flows

import android.app.Activity
import android.content.Intent
import com.simprints.id.domain.modality.ModalityResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory.buildFaceRequest
import com.simprints.id.domain.moduleapi.face.FaceToDomainResponse.fromFaceToDomainResponse
import com.simprints.id.domain.moduleapi.face.requests.DomainToFaceRequest.fromDomainToFaceRequest
import com.simprints.id.orchestrator.modality.ModalityStepRequest
import com.simprints.id.orchestrator.modality.flows.interfaces.SingleModalityFlow
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse

class FaceModalityFlow(private val appRequest: AppRequest,
                       private val packageName: String) : SingleModalityFlowBase(), SingleModalityFlow {

    companion object {
        const val faceActivityClassName = "com.simprints.face.activities.FaceCaptureActivity"
        const val REQUEST_CODE_FACE = 1
    }

    override val intentRequestCode: Int = REQUEST_CODE_FACE

    override fun getNextModalityStepRequest(): ModalityStepRequest {
        val intent = Intent().setClassName(packageName, faceActivityClassName)
        intent.putExtra(IFaceRequest.BUNDLE_KEY, fromDomainToFaceRequest(buildFaceRequest(appRequest)))
        return ModalityStepRequest(intentRequestCode, intent)
    }

    override fun extractModalityResponse(requestCode: Int, resultCode: Int, data: Intent?): ModalityResponse {
        require(resultCode == Activity.RESULT_OK)
        require(data != null)

        val potentialFaceResponse = data.getParcelableExtra<IFaceResponse>(IFaceResponse.BUNDLE_KEY)
        return fromFaceToDomainResponse(potentialFaceResponse)
    }
}
