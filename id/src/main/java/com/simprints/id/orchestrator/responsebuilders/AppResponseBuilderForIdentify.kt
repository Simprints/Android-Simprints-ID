package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppIdentifyResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.entities.MatchConfidence.Companion.computeMatchConfidence
import com.simprints.id.domain.moduleapi.app.responses.entities.MatchResult
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier
import com.simprints.id.domain.moduleapi.face.responses.FaceMatchResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceMatchResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintMatchResult
import com.simprints.id.orchestrator.steps.Step
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.domain.models.ProjectConfiguration

class AppResponseBuilderForIdentify(private val projectConfiguration: ProjectConfiguration) :
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
        val faceResponse = getFaceResponseForIdentify(results)
        val fingerprintResponse = getFingerprintResponseForMatching(results)

        return when {
            fingerprintResponse != null && faceResponse != null -> {
                buildAppIdentifyResponseForFaceAndFinger(fingerprintResponse, sessionId)
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

    private fun buildAppIdentifyResponseForFaceAndFinger(
        fingerprintResponse: FingerprintMatchResponse,
        sessionId: String
    ) =
        AppIdentifyResponse(
            getSortedIdentificationsForFingerprint(fingerprintResponse),
            sessionId
        )

    private fun getSortedIdentificationsForFingerprint(fingerprintResponse: FingerprintMatchResponse) =
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
        }

    private fun buildAppIdentifyResponseForFingerprint(
        fingerprintResponse: FingerprintMatchResponse,
        sessionId: String
    ): AppIdentifyResponse {
        val resultSortedByConfidence = buildResultsFromFingerprintMatchResponse(fingerprintResponse)

        return AppIdentifyResponse(resultSortedByConfidence.map {
            MatchResult(
                it.personId,
                it.confidenceScore.toInt(),
                Tier.computeTier(it.confidenceScore),
                computeMatchConfidence(
                    it.confidenceScore.toInt(),
                    projectConfiguration.fingerprint!!.decisionPolicy
                )
            )
        }, sessionId)
    }

    private fun buildAppIdentifyResponseForFace(
        faceResponse: FaceMatchResponse,
        sessionId: String
    ): AppIdentifyResponse {
        val resultsSortedByConfidence = buildResultsFromFaceMatchResponse(faceResponse)

        return AppIdentifyResponse(
            resultsSortedByConfidence.map {
                MatchResult(
                    it.guidFound,
                    it.confidence.toInt(),
                    Tier.computeTier(it.confidence),
                    computeMatchConfidence(
                        it.confidence.toInt(),
                        projectConfiguration.face!!.decisionPolicy
                    )
                )
            },
            sessionId
        )
    }

    private fun buildResultsFromFingerprintMatchResponse(fingerprintResponse: FingerprintMatchResponse): List<FingerprintMatchResult> {

        val lowFilteredResults = fingerprintResponse.result.filter {
            it.confidenceScore > projectConfiguration.fingerprint!!.decisionPolicy.low
        }.take(projectConfiguration.identification.maxNbOfReturnedCandidates)
            .sortedByDescending { it.confidenceScore }

        return getFingerprintFilteredResultsWithHighConfidence(lowFilteredResults).ifEmpty {
            lowFilteredResults
        }
    }

    private fun getFingerprintFilteredResultsWithHighConfidence(lowFilteredResults: List<FingerprintMatchResult>): List<FingerprintMatchResult> =
        lowFilteredResults.filter {
            it.confidenceScore >= projectConfiguration.fingerprint!!.decisionPolicy.high
        }

    private fun buildResultsFromFaceMatchResponse(faceResponse: FaceMatchResponse): List<FaceMatchResult> {

        val lowFilteredResults = faceResponse.result.filter {
            it.confidence >= projectConfiguration.face!!.decisionPolicy.low
        }.take(projectConfiguration.identification.maxNbOfReturnedCandidates)
            .sortedByDescending { it.confidence }

        return getFaceFilteredResultsWithHighConfidence(lowFilteredResults).ifEmpty {
            lowFilteredResults
        }
    }

    private fun getFaceFilteredResultsWithHighConfidence(lowFilteredResults: List<FaceMatchResult>): List<FaceMatchResult> =
        lowFilteredResults.filter {
            it.confidence >= projectConfiguration.face!!.decisionPolicy.high
        }
}
