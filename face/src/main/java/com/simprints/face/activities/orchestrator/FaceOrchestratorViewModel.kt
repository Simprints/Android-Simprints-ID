package com.simprints.face.activities.orchestrator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.send
import com.simprints.face.data.moduleapi.face.DomainToFaceResponse
import com.simprints.face.data.moduleapi.face.FaceToDomainRequest
import com.simprints.face.data.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.face.data.moduleapi.face.requests.FaceRequest
import com.simprints.face.data.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.face.data.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.face.data.moduleapi.face.responses.entities.FaceSample
import com.simprints.face.data.moduleapi.face.responses.entities.SecuredImageRef
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import com.simprints.core.livedata.LiveDataEventWithContent
import java.util.*

class FaceOrchestratorViewModel : ViewModel() {
    lateinit var faceRequest: FaceRequest

    val startCapture: MutableLiveData<LiveDataEvent> = MutableLiveData()
    val captureFinished: MutableLiveData<LiveDataEventWithContent<IFaceResponse>> = MutableLiveData()

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
        captureFinished.send(DomainToFaceResponse.fromDomainToFaceResponse(generateFakeCaptureResponse()))
    }

    private fun generateFakeCaptureResponse(): FaceCaptureResponse {
        val securedImageRef = SecuredImageRef("file://someFile")
        val sample = FaceSample(UUID.randomUUID().toString(), ByteArray(0), securedImageRef)
        val result = FaceCaptureResult(0, sample)
        val captureResults = listOf(result)
        return FaceCaptureResponse(captureResults)
    }

}
