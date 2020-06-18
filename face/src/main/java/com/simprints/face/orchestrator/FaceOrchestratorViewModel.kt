package com.simprints.face.orchestrator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.face.controllers.core.crashreport.FaceCrashReportManager
import com.simprints.face.controllers.core.crashreport.FaceCrashReportTag.FACE_LICENSE
import com.simprints.face.controllers.core.crashreport.FaceCrashReportTrigger.UI
import com.simprints.face.data.moduleapi.face.DomainToFaceResponse
import com.simprints.face.data.moduleapi.face.FaceToDomainRequest
import com.simprints.face.data.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.face.data.moduleapi.face.requests.FaceMatchRequest
import com.simprints.face.data.moduleapi.face.requests.FaceRequest
import com.simprints.face.data.moduleapi.face.responses.FaceResponse
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import timber.log.Timber

class FaceOrchestratorViewModel(private val crashReportManager: FaceCrashReportManager) :
    ViewModel() {
    lateinit var faceRequest: FaceRequest

    val startCapture: MutableLiveData<LiveDataEventWithContent<FaceCaptureRequest>> =
        MutableLiveData()
    val startMatching: MutableLiveData<LiveDataEventWithContent<FaceMatchRequest>> =
        MutableLiveData()

    val flowFinished: MutableLiveData<LiveDataEventWithContent<IFaceResponse>> = MutableLiveData()

    val missingLicenseEvent: MutableLiveData<LiveDataEvent> = MutableLiveData()
    val invalidLicenseEvent: MutableLiveData<LiveDataEvent> = MutableLiveData()

    fun start(iFaceRequest: IFaceRequest) {
        val request = FaceToDomainRequest.fromFaceToDomainRequest(iFaceRequest)
        when (request) {
            is FaceCaptureRequest -> startCapture.send(request)
            is FaceMatchRequest -> startMatching.send(request)
        }
        faceRequest = request
    }

    fun captureFinished(faceCaptureResponse: FaceResponse?) {
        if (faceCaptureResponse == null) {
            flowFinished.value = null
        } else {
            flowFinished.send(DomainToFaceResponse.fromDomainToFaceResponse(faceCaptureResponse))
        }
    }

    fun matchFinished(faceCaptureResponse: FaceResponse?) {
        if (faceCaptureResponse == null) {
            flowFinished.value = null
        } else {
            flowFinished.send(DomainToFaceResponse.fromDomainToFaceResponse(faceCaptureResponse))
        }
    }

    fun missingLicense() {
        Timber.d("License is missing")
        crashReportManager.logMessageForCrashReport(
            FACE_LICENSE,
            UI,
            message = "License is missing"
        )
        missingLicenseEvent.send()
    }

    fun invalidLicense() {
        Timber.d("License is invalid")
        crashReportManager.logMessageForCrashReport(
            FACE_LICENSE,
            UI,
            message = "License is invalid"
        )
        invalidLicenseEvent.send()
    }

}
