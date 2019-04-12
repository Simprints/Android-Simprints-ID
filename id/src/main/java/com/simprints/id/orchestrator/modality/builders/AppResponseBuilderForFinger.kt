package com.simprints.id.orchestrator.modals.builders

import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.app.responses.*
import com.simprints.id.domain.moduleapi.app.responses.entities.RefusalFormAnswer
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintIdentifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintRefusalFormResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintVerifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.toAppMatchResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.toAppRefusalFormReason
import com.simprints.id.exceptions.unexpected.InvalidAppRequest

class AppResponseBuilderForFinger : AppResponseBuilderForModal {

    override fun buildResponse(appRequest: AppRequest,
                               modalResponses: List<ModalResponse>,
                               sessionId: String): AppResponse {

        val fingerResponse = modalResponses.first()
        if (fingerResponse is FingerprintRefusalFormResponse)
            return buildAppRefusalFormResponse(fingerResponse)

        return when (appRequest) {
            is AppEnrolRequest -> buildAppEnrolResponse(fingerResponse as FingerprintEnrolResponse)
            is AppIdentifyRequest -> {
                require(sessionId.isNotEmpty())
                buildAppIdentifyResponse(fingerResponse as FingerprintIdentifyResponse, sessionId)
            }
            is AppVerifyRequest -> buildAppVerifyResponse(fingerResponse as FingerprintVerifyResponse)
            else -> throw InvalidAppRequest()
        }
    }

    private fun buildAppRefusalFormResponse(fingerprintRefusalFormResponse: FingerprintRefusalFormResponse): AppRefusalFormResponse {
        return AppRefusalFormResponse(RefusalFormAnswer(
            fingerprintRefusalFormResponse.reason?.toAppRefusalFormReason(),
            fingerprintRefusalFormResponse.optionalText))
    }

    //TODO: Ignoring face response for now.
    private fun buildAppIdentifyResponse(fingerprintResponse: FingerprintIdentifyResponse, sessionId: String): AppIdentifyResponse =
        AppIdentifyResponse(fingerprintResponse.identifications.map { it.toAppMatchResult() }, sessionId)

    private fun buildAppVerifyResponse(fingerprintResponse: FingerprintVerifyResponse): AppVerifyResponse =
        AppVerifyResponse(fingerprintResponse.matchingResult.toAppMatchResult())

    private fun buildAppEnrolResponse(fingerprintResponse: FingerprintEnrolResponse): AppEnrolResponse =
        AppEnrolResponse(fingerprintResponse.guid)
}
