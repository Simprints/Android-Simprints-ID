package com.simprints.id.orchestrator.modals.flows

import android.app.Activity
import android.content.Intent
import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory.buildFaceRequest
import com.simprints.id.domain.moduleapi.face.FaceToDomainResponse.fromFaceToDomainResponse
import com.simprints.id.domain.moduleapi.face.requests.DomainToFaceRequest.fromDomainToFaceRequest
import com.simprints.id.orchestrator.modals.ModalStepRequest
import com.simprints.id.orchestrator.modals.flows.interfaces.SingleModalFlow
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse

class FaceModalFlow(private val appRequest: AppRequest,
                    private val packageName: String) : SingleModalFlowBase(), SingleModalFlow {

    companion object {
        const val faceActivityClassName = "com.simprints.face.activities.FaceCaptureActivity"
        const val REQUEST_CODE_FACE = 1
    }

    override val intentRequestCode: Int = REQUEST_CODE_FACE

    override fun getNextModalStepRequest(): ModalStepRequest {
        val intent = Intent().setClassName(packageName, faceActivityClassName)
        intent.putExtra(IFaceRequest.BUNDLE_KEY, fromDomainToFaceRequest(buildFaceRequest(appRequest)))
        return ModalStepRequest(intentRequestCode, intent)
    }

    override fun extractModalResponse(requestCode: Int, resultCode: Int, data: Intent?): ModalResponse {
        require(resultCode == Activity.RESULT_OK)
        require(data != null)

        val potentialFaceResponse = data.getParcelableExtra<IFaceResponse>(IFaceResponse.BUNDLE_KEY)
        return fromFaceToDomainResponse(potentialFaceResponse)
    }
}
