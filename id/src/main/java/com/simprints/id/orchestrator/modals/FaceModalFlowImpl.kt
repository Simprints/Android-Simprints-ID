package com.simprints.id.orchestrator.modals

import android.app.Activity
import android.content.Intent
import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory.buildFaceRequest
import com.simprints.id.domain.moduleapi.face.FaceToDomainResponse.fromFaceToDomainResponse
import com.simprints.id.domain.moduleapi.face.requests.DomainToFaceRequest.fromDomainToFaceRequest
import com.simprints.id.orchestrator.ModalStepRequest
import com.simprints.id.orchestrator.modals.ModalFlowIntentRequestCodes.REQUEST_CODE_FACE
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import io.reactivex.Observable
import io.reactivex.ObservableEmitter

class FaceModalFlowImpl(private val packageName: String,
                        private val appRequest: AppRequest) : ModalFlow {

    companion object {
        const val launchFaceClassName = "com.simprints.face.activities.FaceCaptureActivity"
    }

    private lateinit var responsesEmitter: ObservableEmitter<ModalResponse>
    override var modalResponses: Observable<ModalResponse> = Observable.create {
        responsesEmitter = it
    }

    private lateinit var nextIntentEmitter: ObservableEmitter<ModalStepRequest>
    override var nextIntent: Observable<ModalStepRequest> = Observable.create {
        nextIntentEmitter = it
        nextIntentEmitter.onNext(getNextIntent())
    }

    private fun getNextIntent(): ModalStepRequest {
        val intent = Intent().setClassName(packageName, launchFaceClassName)
        intent.putExtra(IFaceRequest.BUNDLE_KEY, fromDomainToFaceRequest(buildFaceRequest(appRequest)))
        return ModalStepRequest(REQUEST_CODE_FACE, intent)
    }

    @Throws(IllegalArgumentException::class)
    override fun handleIntentResponse(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            val potentialModalResponse = extractModalResponse(data)
            require(resultCode == Activity.RESULT_OK)
            require(potentialModalResponse != null)

            responsesEmitter.onNext(potentialModalResponse)
            responsesEmitter.onComplete()
            nextIntentEmitter.onComplete()
        } catch (t: Throwable) {
            t.printStackTrace()
            responsesEmitter.onError(t)
            nextIntentEmitter.onError(t)
        }
    }

    private fun extractModalResponse(data: Intent?): ModalResponse? {
        val potentialFaceResponse = data?.getParcelableExtra<IFaceResponse>(IFaceResponse.BUNDLE_KEY)
        require(potentialFaceResponse != null)

        return fromFaceToDomainResponse(potentialFaceResponse)
    }
}
