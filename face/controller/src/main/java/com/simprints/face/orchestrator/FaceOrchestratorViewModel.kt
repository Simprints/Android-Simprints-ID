package com.simprints.face.orchestrator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FaceOrchestratorViewModel @Inject constructor() : ViewModel() {
    val startCapture: MutableLiveData<LiveDataEventWithContent<FaceCaptureRequest>> =
        MutableLiveData()
    val startMatching: MutableLiveData<LiveDataEventWithContent<FaceMatchRequest>> =
        MutableLiveData()
    val startConfiguration: MutableLiveData<LiveDataEventWithContent<FaceConfigurationRequest>> =
        MutableLiveData()

    val flowFinished: MutableLiveData<LiveDataEventWithContent<IFaceResponse>> = MutableLiveData()

    val errorEvent: MutableLiveData<LiveDataEventWithContent<ErrorType>> = MutableLiveData()

    fun start(iFaceRequest: IFaceRequest) {
        when (val request = FaceToDomainRequest.fromFaceToDomainRequest(iFaceRequest)) {
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

    fun configurationFinished(
        isSuccess: Boolean,
        errorTitle: String? = null,
        errorMessage: String? = null,
    ) {
        if (isSuccess) {
            flowFinished.send(
                DomainToFaceResponse.fromDomainToFaceResponse(
                    FaceConfigurationResponse()
                )
            )
        } else if (errorTitle != null && errorMessage == null) {
            Simber.tag(CrashReportTag.FACE_LICENSE.name)
                .i("Error with configuration download. Error = $errorTitle")

            errorEvent.send(ErrorType.CONFIGURATION_ERROR.apply {
                this.customTitle =  errorTitle
            })
        } else {
            Simber.tag(CrashReportTag.FACE_LICENSE.name)
                .i("Error with configuration download. The backend is under maintenance")
            errorEvent.send(ErrorType.BACKEND_MAINTENANCE_ERROR.apply {
                this.customMessage = errorMessage
            })
        }
    }

    fun unexpectedErrorHappened() {
        errorEvent.send(ErrorType.UNEXPECTED_ERROR)
    }

}
