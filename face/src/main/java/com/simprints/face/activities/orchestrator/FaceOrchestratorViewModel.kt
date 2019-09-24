package com.simprints.face.activities.orchestrator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.send
import com.simprints.face.data.moduleapi.face.FaceToDomainRequest
import com.simprints.face.data.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.face.data.moduleapi.face.requests.FaceRequest
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import com.simprints.scarecrow.LiveDataEvent1

class FaceOrchestratorViewModel : ViewModel() {
    lateinit var faceRequest: FaceRequest

    val startCapture: MutableLiveData<LiveDataEvent> = MutableLiveData()
    val captureFinished: MutableLiveData<LiveDataEvent1<IFaceResponse>> = MutableLiveData()

    fun start(iFaceRequest: IFaceRequest) {
        faceRequest = FaceToDomainRequest.fromFaceToDomainRequest(iFaceRequest)
        when (faceRequest) {
            is FaceCaptureRequest -> {
                captureNeededPhotos(faceRequest as FaceCaptureRequest)
            }
        }
    }

    // TODO capture the correct number of photos the interface requests when integrating the face modality in full
    private fun captureNeededPhotos(faceCaptureRequest: FaceCaptureRequest) {
        startCapture.send()
    }

    fun captureFinished() {
    }
}
