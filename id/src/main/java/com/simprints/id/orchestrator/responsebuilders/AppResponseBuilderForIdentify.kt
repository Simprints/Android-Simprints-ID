package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppIdentifyResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.entities.MatchConfidence.Companion.computeMatchConfidenceForFace
import com.simprints.id.domain.moduleapi.app.responses.entities.MatchConfidence.Companion.computeMatchConfidenceForFingerprint
import com.simprints.id.domain.moduleapi.app.responses.entities.MatchResult
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier
import com.simprints.id.domain.moduleapi.face.responses.FaceMatchResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceMatchResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintMatchResult
import com.simprints.id.orchestrator.steps.Step

class AppResponseBuilderForIdentify(private val fingerprintConfidenceThresholds: Map<FingerprintConfidenceThresholds, Int>,
                                    private val faceConfidenceThresholds: Map<FaceConfidenceThresholds, Int>,
                                    private val returnIdentificationsCount: Int) : BaseAppResponseBuilder() {

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
            getSortedIdentificationsForFingerprint(fingerprintResponse),
            sessionId)

    private fun getSortedIdentificationsForFingerprint(fingerprintResponse: FingerprintMatchResponse) =
        fingerprintResponse.result.map {
            MatchResult(it.personId,
                it.confidenceScore.toInt(),
                Tier.computeTier(it.confidenceScore),
                computeMatchConfidenceForFingerprint(it.confidenceScore.toInt(), fingerprintConfidenceThresholds))
        }

    private fun buildAppIdentifyResponseForFingerprint(fingerprintResponse: FingerprintMatchResponse,
                                                       sessionId: String): AppIdentifyResponse {
        val resultSortedByConfidence = buildResultsFromFingerprintMatchResponse(fingerprintResponse)

        return AppIdentifyResponse(resultSortedByConfidence.map {
            MatchResult(it.personId,
                it.confidenceScore.toInt(),
                Tier.computeTier(it.confidenceScore),
                computeMatchConfidenceForFingerprint(it.confidenceScore.toInt(), fingerprintConfidenceThresholds))
        }, sessionId)
    }

    private fun buildAppIdentifyResponseForFace(faceResponse: FaceMatchResponse,
                                                sessionId: String): AppIdentifyResponse {
        val resultsSortedByConfidence = buildResultsFromFaceMatchResponse(faceResponse)

        return AppIdentifyResponse(
            resultsSortedByConfidence.map {
                MatchResult(it.guidFound,
                    it.confidence.toInt(),
                    Tier.computeTier(it.confidence),
                    computeMatchConfidenceForFace(it.confidence.toInt(), faceConfidenceThresholds))
            },
            sessionId
        )
    }

    private fun buildResultsFromFingerprintMatchResponse(fingerprintResponse: FingerprintMatchResponse): List<FingerprintMatchResult> {

        val lowFilteredResults = fingerprintResponse.result.filter {
            it.confidenceScore > fingerprintConfidenceThresholds.getValue(FingerprintConfidenceThresholds.LOW)
        }.take(returnIdentificationsCount).sortedByDescending { it.confidenceScore }

        return getFingerprintFilteredResultsWithHighConfidence(lowFilteredResults).ifEmpty {
            lowFilteredResults
        }
    }

    private fun getFingerprintFilteredResultsWithHighConfidence(lowFilteredResults: List<FingerprintMatchResult>): List<FingerprintMatchResult> =
        lowFilteredResults.filter {
            it.confidenceScore >= fingerprintConfidenceThresholds.getValue(FingerprintConfidenceThresholds.HIGH)
        }

    private fun buildResultsFromFaceMatchResponse(faceResponse: FaceMatchResponse): List<FaceMatchResult> {

        val lowFilteredResults = faceResponse.result.filter {
            it.confidence >= faceConfidenceThresholds.getValue(FaceConfidenceThresholds.LOW)
        }.take(returnIdentificationsCount).sortedByDescending { it.confidence }

        return getFaceFilteredResultsWithHighConfidence(lowFilteredResults).ifEmpty {
            lowFilteredResults
        }
    }

    private fun getFaceFilteredResultsWithHighConfidence(lowFilteredResults: List<FaceMatchResult>): List<FaceMatchResult> =
        lowFilteredResults.filter {
            it.confidence >= faceConfidenceThresholds.getValue(FaceConfidenceThresholds.HIGH)
        }
}
