package com.simprints.feature.orchestrator.usecases.response

import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.events.SessionEventRepository
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
    ): AppResponse {
        val currentSessionId = eventRepository.getCurrentSessionScope().id

        val faceResults = getFaceMatchResults(results, projectConfiguration)
        val bestFaceConfidence = faceResults.firstOrNull()?.confidenceScore ?: 0

        val fingerprintResults = getFingerprintResults(results, projectConfiguration)
        val bestFingerprintConfidence = fingerprintResults.firstOrNull()?.confidenceScore ?: 0

        return AppIdentifyResponse(
            sessionId = currentSessionId,
            // Return the results with the highest confidence score
            identifications = if (bestFingerprintConfidence > bestFaceConfidence) {
                fingerprintResults
            } else {
                faceResults
            },
        )
    }

    private fun getFingerprintResults(
        results: List<Serializable>,
        projectConfiguration: ProjectConfiguration,
    ) =  results.filterIsInstance<FingerprintMatchResult>().lastOrNull()?.let { fingerprintMatchResult ->
            projectConfiguration.fingerprint?.getSdkConfiguration(fingerprintMatchResult.sdk)
                ?.decisionPolicy?.let { fingerprintDecisionPolicy ->
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
