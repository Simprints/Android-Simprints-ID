package com.simprints.id.orchestrator.builders

import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.app.responses.AppIdentifyResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.AppVerifyResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceEnrolResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceIdentifyResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceVerifyResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.toAppMatchResult
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.orchestrator.steps.Step

class AppResponseBuilderForFace : AppResponseBuilderForModal {

    override fun buildResponse(appRequest: AppRequest,
                               steps: List<Step>,
                               sessionId: String): AppResponse {

        val results = steps.map { it.result }
        val faceResponse = results.first()

        return when (appRequest) {
            is AppEnrolRequest -> buildAppEnrolResponse(faceResponse as FaceEnrolResponse)
            is AppIdentifyRequest -> {
                require(sessionId.isNotEmpty())
                buildAppIdentifyResponse(faceResponse as FaceIdentifyResponse, sessionId)
            }
            is AppVerifyRequest -> buildAppVerifyResponse(faceResponse as FaceVerifyResponse)
            else -> throw InvalidAppRequest()
        }
    }

    private fun buildAppIdentifyResponse(FaceResponse: FaceIdentifyResponse, sessionId: String): AppIdentifyResponse =
        AppIdentifyResponse(FaceResponse.identifications.map { it.toAppMatchResult() }, sessionId)

    private fun buildAppVerifyResponse(FaceResponse: FaceVerifyResponse): AppVerifyResponse =
        AppVerifyResponse(FaceResponse.matchingResult.toAppMatchResult())

    private fun buildAppEnrolResponse(FaceResponse: FaceEnrolResponse): AppEnrolResponse =
        AppEnrolResponse(FaceResponse.guid)
}
