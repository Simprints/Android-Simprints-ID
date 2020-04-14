package com.simprints.face.orchestrator

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
import com.simprints.face.data.moduleapi.face.responses.entities.FaceMatchResult
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import java.util.*

class FaceOrchestratorViewModel : ViewModel() {
    lateinit var faceRequest: FaceRequest

    val startCapture: MutableLiveData<LiveDataEventWithContent<FaceCaptureRequest>> = MutableLiveData()
    val startMatching: MutableLiveData<LiveDataEvent> = MutableLiveData()

    val flowFinished: MutableLiveData<LiveDataEventWithContent<IFaceResponse>> = MutableLiveData()

    fun start(iFaceRequest: IFaceRequest) {
        val request = FaceToDomainRequest.fromFaceToDomainRequest(iFaceRequest)
        when (request) {
            is FaceCaptureRequest -> startCapture.send(request)
            is FaceMatchRequest -> startMatching.send()
        }
        faceRequest = request
    }

    fun captureFinished(faceCaptureResponse: FaceCaptureResponse?) {
        if (faceCaptureResponse == null) {
            flowFinished.value = null
        } else {
            flowFinished.send(DomainToFaceResponse.fromDomainToFaceResponse(faceCaptureResponse))
        }
    }

    fun matchFinished() {
        val fakeMatchResponse = generateFaceMatchResponse()
        flowFinished.send(DomainToFaceResponse.fromDomainToFaceResponse(fakeMatchResponse))
    }

    private fun generateFaceMatchResponse(): FaceMatchResponse {
        val faceMatchResults = listOf(
            FaceMatchResult(UUID.randomUUID().toString(), 75f),
            FaceMatchResult(UUID.randomUUID().toString(), 50f),
            FaceMatchResult(UUID.randomUUID().toString(), 25f)
        )

        return FaceMatchResponse(faceMatchResults)
    }

}
