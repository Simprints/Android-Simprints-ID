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

        val faceDecisionPolicy = projectConfiguration.face?.decisionPolicy
        val faceResults = getFaceMatchResults(faceDecisionPolicy, results, projectConfiguration)
        val bestFaceConfidence = faceResults.firstOrNull()?.confidenceScore ?: 0

        val fingerprintDecisionPolicy =
            projectConfiguration.fingerprint?.bioSdkConfiguration?.decisionPolicy
        val fingerprintResults =
            getFingerprintResults(fingerprintDecisionPolicy, results, projectConfiguration)
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
        fingerprintDecisionPolicy: DecisionPolicy?,
        results: List<Serializable>,
        projectConfiguration: ProjectConfiguration,
    ) = if (fingerprintDecisionPolicy != null) {
        val matches = results.filterIsInstance(FingerprintMatchResult::class.java).lastOrNull()?.results.orEmpty()
        val goodResults = matches
            .filter { it.confidence >= fingerprintDecisionPolicy.low }
            .sortedByDescending { it.confidence }
        // Attempt to include only high confidence matches
        goodResults
            .filter { it.confidence >= fingerprintDecisionPolicy.high }
            .ifEmpty { goodResults }
            .take(projectConfiguration.identification.maxNbOfReturnedCandidates)
            .map { AppMatchResult(it.subjectId, it.confidence, fingerprintDecisionPolicy) }
    } else emptyList()

    private fun getFaceMatchResults(
        faceDecisionPolicy: DecisionPolicy?,
        results: List<Serializable>,
        projectConfiguration: ProjectConfiguration,
    ) = if (faceDecisionPolicy != null) {
        val matches = results.filterIsInstance(FaceMatchResult::class.java).lastOrNull()?.results.orEmpty()
        val goodResults = matches
            .filter { it.confidence >= faceDecisionPolicy.low }
            .sortedByDescending { it.confidence }
        // Attempt to include only high confidence matches
        goodResults
            .filter { it.confidence >= faceDecisionPolicy.high }
            .ifEmpty { goodResults }
            .take(projectConfiguration.identification.maxNbOfReturnedCandidates)
            .map { AppMatchResult(it.subjectId, it.confidence, faceDecisionPolicy) }
    } else emptyList()
}
