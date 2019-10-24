package com.simprints.face.activities.orchestrator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.face.data.moduleapi.face.DomainToFaceResponse
import com.simprints.face.data.moduleapi.face.FaceToDomainRequest
import com.simprints.face.data.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.face.data.moduleapi.face.requests.FaceMatchRequest
import com.simprints.face.data.moduleapi.face.requests.FaceRequest
import com.simprints.face.data.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.face.data.moduleapi.face.responses.FaceMatchResponse
import com.simprints.face.data.moduleapi.face.responses.entities.*
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import java.util.*

class FaceOrchestratorViewModel : ViewModel() {
    lateinit var faceRequest: FaceRequest

    val startCapture: MutableLiveData<LiveDataEvent> = MutableLiveData()
    val captureFinished: MutableLiveData<LiveDataEventWithContent<IFaceResponse>> = MutableLiveData()

    fun start(iFaceRequest: IFaceRequest) {
        val request = FaceToDomainRequest.fromFaceToDomainRequest(iFaceRequest)
        when (request) {
            is FaceCaptureRequest -> {
                captureNeededPhotos(request)
            }
            is FaceMatchRequest -> {
                generateFaceMatchResponse()
            }
        }
        faceRequest = request
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

    private fun generateFaceMatchResponse(): FaceMatchResponse {

        val faceMatchResults = listOf(
            FaceMatchResult(UUID.randomUUID().toString(), 75, FaceTier.TIER_1),
            FaceMatchResult(UUID.randomUUID().toString(), 50, FaceTier.TIER_2),
            FaceMatchResult(UUID.randomUUID().toString(), 25, FaceTier.TIER_3)
        )

        return FaceMatchResponse(faceMatchResults)
    }

}
