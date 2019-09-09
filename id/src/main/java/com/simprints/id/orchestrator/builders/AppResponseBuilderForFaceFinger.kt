package com.simprints.id.orchestrator.builders

import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.app.responses.*
import com.simprints.id.domain.moduleapi.app.responses.entities.RefusalFormAnswer
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceIdentifyResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceVerifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.*
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.toAppMatchResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.toAppRefusalFormReason
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.orchestrator.steps.Step

class AppResponseBuilderForFaceFinger : AppResponseBuilderForModal {

    companion object {
        const val FINGER_RESPONSE_INDEX = 1
    }

    override fun buildResponse(appRequest: AppRequest,
                               steps: List<Step>,
                               sessionId: String): AppResponse {

        val results = steps.map { it.result }
        val faceResponse = results.first()
        val fingerResponse = results[FINGER_RESPONSE_INDEX]

        if (fingerResponse is FingerprintRefusalFormResponse)
            return buildAppRefusalFormResponse(fingerResponse)

        if (fingerResponse is FingerprintErrorResponse)
            return buildAppErrorResponse(fingerResponse)

        return when (appRequest) {
            is AppEnrolRequest -> buildAppEnrolResponse(fingerResponse as FingerprintEnrolResponse, faceResponse as FaceCaptureResponse)
            is AppIdentifyRequest -> {
                require(sessionId.isNotEmpty())
                buildAppIdentifyResponse(fingerResponse as FingerprintIdentifyResponse, faceResponse as FaceIdentifyResponse, sessionId)
            }
            is AppVerifyRequest -> buildAppVerifyResponse(fingerResponse as FingerprintVerifyResponse, faceResponse as FaceVerifyResponse)
            else -> throw InvalidAppRequest()
        }
    }

    private fun buildAppRefusalFormResponse(fingerprintRefusalFormResponse: FingerprintRefusalFormResponse): AppRefusalFormResponse {
        return AppRefusalFormResponse(RefusalFormAnswer(
            fingerprintRefusalFormResponse.reason.toAppRefusalFormReason(),
            fingerprintRefusalFormResponse.optionalText))
    }

    private fun buildAppErrorResponse(fingerResponse: FingerprintErrorResponse): AppResponse =
        AppErrorResponse(fingerResponse.fingerprintErrorReason.toAppErrorReason())

    //TODO: Ignoring face response for now.
    private fun buildAppIdentifyResponse(fingerprintResponse: FingerprintIdentifyResponse,
                                         faceResponse: FaceIdentifyResponse, sessionId: String): AppIdentifyResponse =
        AppIdentifyResponse(fingerprintResponse.identifications.map { it.toAppMatchResult() }, sessionId)

    private fun buildAppVerifyResponse(fingerprintResponse: FingerprintVerifyResponse,
                                       faceResponse: FaceVerifyResponse): AppVerifyResponse =
        AppVerifyResponse(fingerprintResponse.matchingResult.toAppMatchResult())

    private fun buildAppEnrolResponse(fingerprintResponse: FingerprintEnrolResponse,
                                      faceResponse: FaceCaptureResponse): AppEnrolResponse =
        AppEnrolResponse(fingerprintResponse.guid)
}
