package com.simprints.id.orchestrator.modals.face

import android.app.Activity
import android.content.Intent
import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory.buildFaceRequest
import com.simprints.id.domain.moduleapi.face.FaceToDomainResponse.fromFaceToDomainResponse
import com.simprints.id.domain.moduleapi.face.requests.DomainToFaceRequest.fromDomainToFaceRequest
import com.simprints.id.orchestrator.ModalStepRequest
import com.simprints.id.orchestrator.modals.ModalFlow
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

    private lateinit var emitter: ObservableEmitter<ModalStepRequest>
    override var flow: Observable<ModalStepRequest> = Observable.create {
        emitter = it
        emitter.onNext(getNextIntent())
    }

    private fun getNextIntent(): ModalStepRequest {
        val intent = Intent().setClassName(packageName, launchFaceClassName)
        intent.putExtra(IFaceRequest.BUNDLE_KEY, fromDomainToFaceRequest(buildFaceRequest(appRequest)))
        return ModalStepRequest(REQUEST_CODE_FACE, intent)
    }

    @Throws(IllegalArgumentException::class)
    override fun handleModalResponse(requestCode: Int, resultCode: Int, data: Intent?): Observable<ModalResponse> =
        try {
            val potentialModalResponse = extractModalResponse(data)
            require(resultCode == Activity.RESULT_OK)
            require(potentialModalResponse != null)

            emitter.onComplete()
            Observable.just(potentialModalResponse)
        } catch (t: Throwable) {
            t.printStackTrace()
            emitter.onError(t)
            Observable.error(Throwable("Impossible to create ModalResponse"))
        }


    private fun extractModalResponse(data: Intent?): ModalResponse? {
        val potentialFaceResponse = data?.getParcelableExtra<IFaceResponse>(IFaceResponse.BUNDLE_KEY)
        require(potentialFaceResponse != null)

        return fromFaceToDomainResponse(potentialFaceResponse)
    }
}
