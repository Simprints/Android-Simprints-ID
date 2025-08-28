package com.simprints.feature.orchestrator.usecases.response

import com.simprints.core.domain.modality.Modality
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.getModalityConfigs
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.orchestration.data.responses.AppIdentifyResponse
import com.simprints.infra.orchestration.data.responses.AppMatchResult
import com.simprints.infra.orchestration.data.responses.AppResponse
import com.simprints.matcher.MatchResult
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

        // Processing modalities separately since we should return results from either one
        // based on whichever has the highest confidence score
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
    ) = results
        .filterIsInstance<MatchResult>()
        .lastOrNull { it.modality == Modality.FINGERPRINT }
        ?.let { fingerprintMatchResult ->
            projectConfiguration
                .getModalityConfigs()[Modality.FINGERPRINT]
                ?.get(fingerprintMatchResult.bioSdk)
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
    ) = results
        .filterIsInstance<MatchResult>()
        .lastOrNull { it.modality == Modality.FACE }
        ?.let { faceMatchResult ->
            projectConfiguration
                .getModalityConfigs()[Modality.FACE]
                ?.get(faceMatchResult.bioSdk)
                ?.decisionPolicy
                ?.let { faceDecisionPolicy ->
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
