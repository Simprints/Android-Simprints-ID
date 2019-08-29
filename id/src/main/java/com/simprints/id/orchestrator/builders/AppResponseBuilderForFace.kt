package com.simprints.id.orchestrator.builders

import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.app.responses.AppIdentifyResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.AppVerifyResponse
import com.simprints.id.domain.moduleapi.app.responses.entities.MatchResult
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceIdentifyResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceVerifyResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceMatchingResult
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceTier
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.orchestrator.steps.Step

class AppResponseBuilderForFace : AppResponseBuilderForModal {

    override fun buildResponse(appRequest: AppRequest,
                               steps: List<Step>,
                               sessionId: String): AppResponse {

        val results = steps.map { it.result }
        val faceResponse = results.first()

        return when (appRequest) {
            is AppEnrolRequest ->
                buildAppEnrolResponse(faceResponse as FaceCaptureResponse)

            is AppIdentifyRequest -> {
                require(sessionId.isNotEmpty())
                buildAppIdentifyResponse(faceResponse as FaceIdentifyResponse, sessionId)
            }

            is AppVerifyRequest -> buildAppVerifyResponse(faceResponse as FaceVerifyResponse)

            else -> throw InvalidAppRequest()
        }
    }

    private fun buildAppIdentifyResponse(faceResponse: FaceIdentifyResponse, sessionId: String): AppIdentifyResponse =
        AppIdentifyResponse(faceResponse.identifications.map { it.toAppMatchResult() }, sessionId)

    private fun buildAppVerifyResponse(FaceResponse: FaceVerifyResponse): AppVerifyResponse =
        AppVerifyResponse(FaceResponse.matchingResult.toAppMatchResult())

    private fun buildAppEnrolResponse(faceResponse: FaceCaptureResponse): AppEnrolResponse {
        //TODO(Save in the db and build an app Enrol response) //STOPSHIP
        throw NotImplementedError()
    }
}

private fun FaceMatchingResult.toAppMatchResult() =
    MatchResult(this.guidFound, this.confidence, tier.toAppTier())

private fun FaceTier.toAppTier() =
    when (this) {
        FaceTier.TIER_1 -> Tier.TIER_1
        FaceTier.TIER_2 -> Tier.TIER_2
        FaceTier.TIER_3 -> Tier.TIER_3
        FaceTier.TIER_4 -> Tier.TIER_4
        FaceTier.TIER_5 -> Tier.TIER_5
    }
