package com.simprints.id.orchestrator.modals.builders

import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.app.responses.AppIdentifyResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.AppVerifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintIdentifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintVerifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.toAppMatchResult

class AppResponseForFingerModal : AppResponseBuilderForModal {

    override fun buildResponse(appRequest: AppRequest,
                               modalResponses: List<ModalResponse>,
                               sessionId: String): AppResponse {

        val faceResponse = modalResponses.first()
        return when (appRequest) {
            is AppEnrolRequest -> buildAppEnrolResponse(faceResponse as FingerprintEnrolResponse)
            is AppIdentifyRequest -> {
                require(sessionId.isNotEmpty())
                buildAppIdentifyResponse(faceResponse as FingerprintIdentifyResponse, sessionId)
            }
            is AppVerifyRequest -> buildAppVerifyResponse(faceResponse as FingerprintVerifyResponse)
            else -> throw Throwable("Invalid AppRequest")
        }
    }

    private fun buildAppIdentifyResponse(fingerprintResponse: FingerprintIdentifyResponse, sessionId: String): AppIdentifyResponse =
        AppIdentifyResponse(fingerprintResponse.identifications.map { it.toAppMatchResult() }, sessionId)

    private fun buildAppVerifyResponse(fingerprintResponse: FingerprintVerifyResponse): AppVerifyResponse =
        AppVerifyResponse(fingerprintResponse.matchingResult.toAppMatchResult())

    private fun buildAppEnrolResponse(fingerprintResponse: FingerprintEnrolResponse): AppEnrolResponse =
        AppEnrolResponse(fingerprintResponse.guid)
}
