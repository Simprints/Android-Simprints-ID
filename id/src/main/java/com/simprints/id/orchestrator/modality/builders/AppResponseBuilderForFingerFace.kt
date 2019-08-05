package com.simprints.id.orchestrator.modality.builders

import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.app.responses.*
import com.simprints.id.domain.moduleapi.app.responses.entities.RefusalFormAnswer
import com.simprints.id.domain.moduleapi.face.responses.FaceEnrolResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceIdentifyResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceVerifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.*
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.toAppMatchResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.toAppRefusalFormReason
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow

class AppResponseBuilderForFingerFace : AppResponseBuilderForModal {

    override fun buildResponse(appRequest: AppRequest,
                               modalityRespons: List<ModalityFlow.Result>,
                               sessionId: String): AppResponse {

        val fingerResponse = modalityRespons.first()
        val faceResponse = modalityRespons[1]

        if (fingerResponse is FingerprintRefusalFormResponse)
            return buildAppRefusalFormResponse(fingerResponse)

        if (fingerResponse is FingerprintErrorResponse)
            return buildAppErrorResponse(fingerResponse)

        return when (appRequest) {
            is AppEnrolRequest -> buildAppEnrolResponse(fingerResponse as FingerprintEnrolResponse, faceResponse as FaceEnrolResponse)
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
            fingerprintRefusalFormResponse.reason?.toAppRefusalFormReason(),
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
                                      faceResponse: FaceEnrolResponse): AppEnrolResponse =
        AppEnrolResponse(fingerprintResponse.guid)
}
