package com.simprints.face.orchestrator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.face.data.moduleapi.face.DomainToFaceResponse
import com.simprints.face.data.moduleapi.face.FaceToDomainRequest
import com.simprints.face.data.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.face.data.moduleapi.face.responses.FaceErrorReason
import com.simprints.face.data.moduleapi.face.responses.FaceErrorResponse
import com.simprints.face.data.moduleapi.face.responses.FaceResponse
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FaceOrchestratorViewModel @Inject constructor() : ViewModel() {
    val startCapture: MutableLiveData<LiveDataEventWithContent<FaceCaptureRequest>> =
        MutableLiveData()

    val flowFinished: MutableLiveData<LiveDataEventWithContent<IFaceResponse>> = MutableLiveData()

    val errorEvent: MutableLiveData<LiveDataEvent> = MutableLiveData()

    fun start(iFaceRequest: IFaceRequest) {
        when (val request = FaceToDomainRequest.fromFaceToDomainRequest(iFaceRequest)) {
            is FaceCaptureRequest -> startCapture.send(request)
        }
    }

    fun captureFinished(faceCaptureResponse: FaceResponse?) {
        if (faceCaptureResponse == null) {
            flowFinished.value = null
        } else {
            flowFinished.send(DomainToFaceResponse.fromDomainToFaceResponse(faceCaptureResponse))
        }
    }

    fun finishWithError() {
        flowFinished.send(
            DomainToFaceResponse.fromDomainToFaceResponse(
                FaceErrorResponse(FaceErrorReason.UNEXPECTED_ERROR)
            )
        )
    }

    fun unexpectedErrorHappened() {
        errorEvent.send()
    }

}
