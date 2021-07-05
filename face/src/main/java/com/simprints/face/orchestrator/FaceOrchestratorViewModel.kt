package com.simprints.face.orchestrator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.analytics.CrashReportTag
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.face.data.moduleapi.face.DomainToFaceResponse
import com.simprints.face.data.moduleapi.face.FaceToDomainRequest
import com.simprints.face.data.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.face.data.moduleapi.face.requests.FaceConfigurationRequest
import com.simprints.face.data.moduleapi.face.requests.FaceMatchRequest
import com.simprints.face.data.moduleapi.face.responses.FaceConfigurationResponse
import com.simprints.face.data.moduleapi.face.responses.FaceErrorReason
import com.simprints.face.data.moduleapi.face.responses.FaceErrorResponse
import com.simprints.face.data.moduleapi.face.responses.FaceResponse
import com.simprints.face.error.ErrorType
import com.simprints.logging.Simber
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse

class FaceOrchestratorViewModel : ViewModel() {
    val startCapture: MutableLiveData<LiveDataEventWithContent<FaceCaptureRequest>> =
        MutableLiveData()
    val startMatching: MutableLiveData<LiveDataEventWithContent<FaceMatchRequest>> =
        MutableLiveData()
    val startConfiguration: MutableLiveData<LiveDataEventWithContent<FaceConfigurationRequest>> =
        MutableLiveData()

    val flowFinished: MutableLiveData<LiveDataEventWithContent<IFaceResponse>> = MutableLiveData()

    val errorEvent: MutableLiveData<LiveDataEventWithContent<ErrorType>> = MutableLiveData()

    fun start(iFaceRequest: IFaceRequest) {
        val request = FaceToDomainRequest.fromFaceToDomainRequest(iFaceRequest)
        when (request) {
            is FaceConfigurationRequest -> startConfiguration.send(request)
            is FaceCaptureRequest -> startCapture.send(request)
            is FaceMatchRequest -> startMatching.send(request)
        }
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

    fun finishWithError(errorType: ErrorType) {
        flowFinished.send(
            DomainToFaceResponse.fromDomainToFaceResponse(
                FaceErrorResponse(FaceErrorReason.fromErrorType(errorType))
            )
        )
    }

    fun missingLicense() {
        Simber.tag(CrashReportTag.FACE_LICENSE.name).i("License is missing")
        errorEvent.send(ErrorType.LICENSE_MISSING)
    }

    fun invalidLicense() {
        Simber.tag(CrashReportTag.FACE_LICENSE.name).i("License is invalid")
        errorEvent.send(ErrorType.LICENSE_INVALID)
    }

    fun configurationFinished(isSuccess: Boolean, errorCode: String? = null) {
        if (isSuccess) {
            flowFinished.send(
                DomainToFaceResponse.fromDomainToFaceResponse(
                    FaceConfigurationResponse()
                )
            )
        } else {
            Simber.tag(CrashReportTag.FACE_LICENSE.name)
                .i("Error with configuration download. Error Code = $errorCode")
            errorEvent.send(ErrorType.CONFIGURATION_ERROR.apply {
                this.errorCode = errorCode
            })
        }
    }

    fun unexpectedErrorHappened() {
        errorEvent.send(ErrorType.UNEXPECTED_ERROR)
    }

}
