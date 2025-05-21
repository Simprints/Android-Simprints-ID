package com.simprints.feature.orchestrator.usecases.response

import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.orchestration.data.responses.AppIdentifyResponse
import com.simprints.infra.orchestration.data.responses.AppMatchResult
import com.simprints.infra.orchestration.data.responses.AppResponse
import com.simprints.matcher.FaceMatchResult
import com.simprints.matcher.FingerprintMatchResult
import java.io.Serializable
import javax.inject.Inject

internal class CreateIdentifyResponseUseCase @Inject constructor(
    private val eventRepository: SessionEventRepository,
) {
    suspend operator fun invoke(
        projectConfiguration: ProjectConfiguration,
        results: List<Serializable>,
        shouldReturnSearchAndVerifyFlag : Boolean,
    ): AppResponse {
        val currentSessionId = eventRepository.getCurrentSessionScope().id

        val faceResults = getFaceMatchResults(results, projectConfiguration)
        val bestFaceConfidence = faceResults.firstOrNull()?.confidenceScore ?: 0

        val fingerprintResults = getFingerprintResults(results, projectConfiguration)
        val bestFingerprintConfidence = fingerprintResults.firstOrNull()?.confidenceScore ?: 0

        // Return the results with the highest confidence score
        val identifications = if (bestFingerprintConfidence > bestFaceConfidence) {
            fingerprintResults
        } else {
            faceResults
        }
        // [MS-992] 'searchAndVerifyMatched' flag should only be returned if 'Search & Verify' was used (1:1 match), and the identification
        // result contains a single item. Flag should be null and in any other case
        val searchAndVerifyMatched = if (shouldReturnSearchAndVerifyFlag) {
            identifications.size == 1
        } else null
        return AppIdentifyResponse(
            sessionId = currentSessionId,
            identifications = identifications,
            searchAndVerifyMatched = searchAndVerifyMatched,
        )
    }

    private fun getFingerprintResults(
        results: List<Serializable>,
        projectConfiguration: ProjectConfiguration,
    ) = results.filterIsInstance<FingerprintMatchResult>().lastOrNull()?.let { fingerprintMatchResult ->
        projectConfiguration.fingerprint
            ?.getSdkConfiguration(fingerprintMatchResult.sdk)
            ?.decisionPolicy
            ?.let { fingerprintDecisionPolicy ->
                val matches = fingerprintMatchResult.results
                val goodResults = matches
                    .filter { it.confidence >= fingerprintDecisionPolicy.low }
                    .sortedByDescending { it.confidence }
                // Attempt to include only high confidence matches
                goodResults
                    .filter { it.confidence >= fingerprintDecisionPolicy.high }
                    .ifEmpty { goodResults }
                    .take(projectConfiguration.identification.maxNbOfReturnedCandidates)
                    .map { AppMatchResult(it.subjectId, it.confidence, fingerprintDecisionPolicy) }
            }
    } ?: emptyList()

    private fun getFaceMatchResults(
        results: List<Serializable>,
        projectConfiguration: ProjectConfiguration,
    ) = results.filterIsInstance<FaceMatchResult>().lastOrNull()?.let { faceMatchResult ->
        projectConfiguration.face?.decisionPolicy?.let { faceDecisionPolicy ->
            val matches = faceMatchResult.results
            val goodResults = matches
                .filter { it.confidence >= faceDecisionPolicy.low }
                .sortedByDescending { it.confidence }
            // Attempt to include only high confidence matches
            goodResults
                .filter { it.confidence >= faceDecisionPolicy.high }
                .ifEmpty { goodResults }
                .take(projectConfiguration.identification.maxNbOfReturnedCandidates)
                .map { AppMatchResult(it.subjectId, it.confidence, faceDecisionPolicy) }
        }
    } ?: emptyList()
}
