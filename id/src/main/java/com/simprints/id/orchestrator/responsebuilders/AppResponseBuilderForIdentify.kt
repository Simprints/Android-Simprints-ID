package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppIdentifyResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.entities.MatchResult
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier
import com.simprints.id.domain.moduleapi.face.responses.FaceMatchResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
import com.simprints.id.orchestrator.steps.Step

class AppResponseBuilderForIdentify : BaseAppResponseBuilder() {

    override suspend fun buildAppResponse(modalities: List<Modality>,
                                          appRequest: AppRequest,
                                          steps: List<Step>,
                                          sessionId: String): AppResponse {
        super.getErrorOrRefusalResponseIfAny(steps)?.let {
            return it
        }

        val results = steps.map { it.getResult() }
        val faceResponse = getFaceResponseForIdentify(results)
        val fingerprintResponse = getFingerprintResponseForMatching(results)

        return when {
            fingerprintResponse != null && faceResponse != null -> {
                buildAppIdentifyResponseForFaceAndFinger(faceResponse, fingerprintResponse, sessionId)
            }
            fingerprintResponse != null -> {
                buildAppIdentifyResponseForFingerprint(fingerprintResponse, sessionId)
            }
            faceResponse != null -> {
                buildAppIdentifyResponseForFace(faceResponse, sessionId)
            }
            else -> throw Throwable("All responses are null")
        }
    }

    private fun getFaceResponseForIdentify(results: List<Step.Result?>): FaceMatchResponse? =
        results.filterIsInstance(FaceMatchResponse::class.java).lastOrNull()

    private fun getFingerprintResponseForMatching(results: List<Step.Result?>): FingerprintMatchResponse? =
        results.filterIsInstance(FingerprintMatchResponse::class.java).lastOrNull()

    private fun buildAppIdentifyResponseForFaceAndFinger(faceResponse: FaceMatchResponse,
                                                         fingerprintResponse: FingerprintMatchResponse,
                                                         sessionId: String) =
        AppIdentifyResponse(
            fingerprintResponse.result.map { MatchResult(it.personId, it.confidenceScore.toInt(), Tier.computeTier(it.confidenceScore)) },
            sessionId)

    private fun buildAppIdentifyResponseForFingerprint(fingerprintResponse: FingerprintMatchResponse,
                                                       sessionId: String) =
        AppIdentifyResponse(
            fingerprintResponse.result.map { MatchResult(it.personId, it.confidenceScore.toInt(), Tier.computeTier(it.confidenceScore)) },
            sessionId)

    private fun buildAppIdentifyResponseForFace(faceResponse: FaceMatchResponse, sessionId: String) =
        AppIdentifyResponse(
            faceResponse.result.map { MatchResult(it.guidFound, it.confidence, Tier.computeTier(it.confidence.toFloat())) },
            sessionId
        )
}
