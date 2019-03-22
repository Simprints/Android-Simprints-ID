package com.simprints.id.orchestrator.modals.builders

import com.simprints.face.data.moduleapi.face.responses.FaceIdentifyResponse
import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.app.responses.*
import com.simprints.id.domain.moduleapi.app.responses.entities.RefusalFormAnswer
import com.simprints.id.domain.moduleapi.face.responses.FaceEnrolResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceVerifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintIdentifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintRefusalFormResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintVerifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.toAppMatchResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.toAppRefusalFormReason

class AppResponseBuilderForFaceFinger : AppResponseBuilderForModal {

    override fun buildResponse(appRequest: AppRequest,
                               modalResponses: List<ModalResponse>,
                               sessionId: String): AppResponse {

        val faceResponse = modalResponses.first()
        val fingerResponse = modalResponses[1]

        if (fingerResponse is FingerprintRefusalFormResponse)
            return buildAppRefusalFormResponse(fingerResponse)

        return when (appRequest) {
            is AppEnrolRequest -> buildAppEnrolResponse(fingerResponse as FingerprintEnrolResponse, faceResponse as FaceEnrolResponse)
            is AppIdentifyRequest -> {
                require(sessionId.isNotEmpty())
                buildAppIdentifyResponse(fingerResponse as FingerprintIdentifyResponse, faceResponse as FaceIdentifyResponse, sessionId)
            }
            is AppVerifyRequest -> buildAppVerifyResponse(fingerResponse as FingerprintVerifyResponse, faceResponse as FaceVerifyResponse)
            else -> throw Throwable("Invalid AppRequest")
        }
    }

    private fun buildAppRefusalFormResponse(fingerprintRefusalFormResponse: FingerprintRefusalFormResponse): AppRefusalFormResponse {
        val fingerprintRefusalFormAnswer = fingerprintRefusalFormResponse.answer
        return AppRefusalFormResponse(RefusalFormAnswer(
            fingerprintRefusalFormAnswer.reason?.toAppRefusalFormReason(),
            fingerprintRefusalFormAnswer.optionalText))
    }

    //TODO: Ignoring face response for now.
    private fun buildAppIdentifyResponse(fingerprintResponse: FingerprintIdentifyResponse,
                                         faceResponse: FaceIdentifyResponse, sessionId: String): AppIdentifyResponse =
        AppIdentifyResponse(fingerprintResponse.identifications.map { it.toAppMatchResult() }, sessionId)

    private fun buildAppVerifyResponse(fingerprintResponse: FingerprintVerifyResponse,
                                       faceResponse: FaceVerifyResponse): AppVerifyResponse =
        AppVerifyResponse(fingerprintResponse.matchingResult.toAppMatchResult())

    private fun buildAppEnrolResponse(fingerprintResponse: FingerprintEnrolResponse,
                                      faceResponse: FaceEnrolResponse): AppEnrolResponse =
        AppEnrolResponse(fingerprintResponse.guid)
}
