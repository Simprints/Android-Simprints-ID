package com.simprints.id.orchestrator.modals.builders

import com.simprints.face.data.moduleapi.face.responses.FaceIdentifyResponse
import com.simprints.face.data.moduleapi.face.responses.entities.toAppMatchResult
import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.app.responses.AppIdentifyResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.AppVerifyResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceEnrolResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceVerifyResponse

class AppResponseForFaceModal : AppResponseBuilderForModal {

    override fun buildResponse(appRequest: AppRequest,
                               modalResponses: List<ModalResponse>,
                               sessionId: String): AppResponse {

        val faceResponse = modalResponses.first()
        return when (appRequest) {
            is AppEnrolRequest -> buildAppEnrolResponse(faceResponse as FaceEnrolResponse)
            is AppIdentifyRequest -> {
                require(sessionId.isNotEmpty())
                buildAppIdentifyResponse(faceResponse as FaceIdentifyResponse, sessionId)
            }
            is AppVerifyRequest -> buildAppVerifyResponse(faceResponse as FaceVerifyResponse)
            else -> throw Throwable("Invalid AppRequest")
        }
    }

    private fun buildAppIdentifyResponse(FaceResponse: FaceIdentifyResponse, sessionId: String): AppIdentifyResponse =
        AppIdentifyResponse(FaceResponse.identifications.map { it.toAppMatchResult() }, sessionId)

    private fun buildAppVerifyResponse(FaceResponse: FaceVerifyResponse): AppVerifyResponse =
        AppVerifyResponse(FaceResponse.matchingResult.toAppMatchResult())

    private fun buildAppEnrolResponse(FaceResponse: FaceEnrolResponse): AppEnrolResponse =
        AppEnrolResponse(FaceResponse.guid)
}
