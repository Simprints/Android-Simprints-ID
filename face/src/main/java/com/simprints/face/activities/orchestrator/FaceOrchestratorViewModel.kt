package com.simprints.face.activities.orchestrator

import androidx.lifecycle.ViewModel
import com.simprints.face.data.moduleapi.face.FaceToDomainRequest
import com.simprints.face.data.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.face.data.moduleapi.face.requests.FaceRequest
import com.simprints.moduleapi.face.requests.IFaceRequest
import timber.log.Timber

class FaceOrchestratorViewModel : ViewModel() {
    lateinit var faceRequest: FaceRequest

    fun start(iFaceRequest: IFaceRequest) {
        faceRequest = FaceToDomainRequest.fromFaceToDomainRequest(iFaceRequest)
        when (faceRequest) {
            is FaceCaptureRequest -> {
                Timber.d(faceRequest.toString())
            }
        }
    }
}
