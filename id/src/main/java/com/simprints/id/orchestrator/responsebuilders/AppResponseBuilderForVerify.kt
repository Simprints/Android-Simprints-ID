package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.AppVerifyResponse
import com.simprints.id.domain.moduleapi.app.responses.entities.MatchConfidence.Companion.computeMatchConfidence
import com.simprints.id.domain.moduleapi.app.responses.entities.MatchResult
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier
import com.simprints.id.domain.moduleapi.face.responses.FaceMatchResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
import com.simprints.id.exceptions.unexpected.MissingCaptureResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration

class AppResponseBuilderForVerify(private val projectConfiguration: ProjectConfiguration) :
    BaseAppResponseBuilder() {

    override suspend fun buildAppResponse(
        modalities: List<GeneralConfiguration.Modality>,
        appRequest: AppRequest,
        steps: List<Step>,
        sessionId: String
    ): AppResponse {
        super.getErrorOrRefusalResponseIfAny(steps)?.let {
            return it
        }

        val results = steps.map { it.getResult() }
        val faceResponse = getFaceResponseForVerify(results)
        val fingerprintResponse = getFingerprintResponseForMatching(results)

        return when {
            fingerprintResponse != null && faceResponse != null -> {
                buildAppVerifyResponseForFingerprintAndFace(
                    fingerprintResponse,
                    faceResponse
                )
            }
            fingerprintResponse != null -> {
                buildAppVerifyResponseForFingerprint(fingerprintResponse)
            }
            faceResponse != null -> {
                buildAppVerifyResponseForFace(faceResponse)
            }
            else -> throw MissingCaptureResponse()
        }
    }

    private fun getFaceResponseForVerify(results: List<Step.Result?>): FaceMatchResponse? =
        results.filterIsInstance(FaceMatchResponse::class.java).lastOrNull()

    private fun getFingerprintResponseForMatching(results: List<Step.Result?>): FingerprintMatchResponse? =
        results.filterIsInstance(FingerprintMatchResponse::class.java).lastOrNull()

    private fun buildAppVerifyResponseForFingerprintAndFace(
        fingerprintResponse: FingerprintMatchResponse,
        faceResponse: FaceMatchResponse,
    ): AppVerifyResponse {
        val fingerprintMatchResult = getMatchResultForFingerprintResponse(fingerprintResponse)
        val faceMatchResult = getMatchResultForFaceResponse(faceResponse)
        val betterMatchResult =
            if (fingerprintMatchResult.confidence > faceMatchResult.confidence)
                fingerprintMatchResult
            else
                faceMatchResult
        return AppVerifyResponse(betterMatchResult)
    }

    private fun buildAppVerifyResponseForFingerprint(fingerprintResponse: FingerprintMatchResponse) =
        AppVerifyResponse(getMatchResultForFingerprintResponse(fingerprintResponse))

    private fun getMatchResultForFingerprintResponse(fingerprintResponse: FingerprintMatchResponse) =
        fingerprintResponse.result.map {
            MatchResult(
                it.personId,
                it.confidenceScore.toInt(),
                Tier.computeTier(it.confidenceScore),
                computeMatchConfidence(
                    it.confidenceScore.toInt(),
                    projectConfiguration.fingerprint!!.decisionPolicy
                )
            )
        }.first()

    private fun buildAppVerifyResponseForFace(faceResponse: FaceMatchResponse) =
        AppVerifyResponse(getMatchResultForFaceResponse(faceResponse))

    private fun getMatchResultForFaceResponse(faceResponse: FaceMatchResponse) =
        faceResponse.result.map {
            MatchResult(
                it.guidFound,
                it.confidence.toInt(),
                Tier.computeTier(it.confidence),
                computeMatchConfidence(
                    it.confidence.toInt(),
                    projectConfiguration.face!!.decisionPolicy
                )
            )
        }.first()
}
