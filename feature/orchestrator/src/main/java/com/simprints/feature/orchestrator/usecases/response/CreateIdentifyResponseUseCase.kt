package com.simprints.feature.orchestrator.usecases.response

import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.getModalitySdkConfig
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

        return AppIdentifyResponse(
            sessionId = currentSessionId,
            // Return the results with the highest confidence score
            identifications = getResults(results, projectConfiguration),
        )
    }

    private fun getResults(
        results: List<Serializable>,
        projectConfiguration: ProjectConfiguration,
    ) = results
        .filterIsInstance<MatchResult>()
        .mapNotNull { matchResult ->
            projectConfiguration
                .getModalitySdkConfig(matchResult.sdk)
                ?.decisionPolicy
                ?.let { policy ->
                    val matches = matchResult.results
                    val goodResults = matches
                        .filter { it.confidence >= policy.low }
                        .sortedByDescending { it.confidence }
                    // Attempt to include only high confidence matches
                    goodResults
                        .filter { it.confidence >= policy.high }
                        .ifEmpty { goodResults }
                        .take(projectConfiguration.identification.maxNbOfReturnedCandidates)
                        .map { AppMatchResult(it.subjectId, it.confidence, policy) }
                }
        }.maxByOrNull { results -> results.maxOf { it.confidenceScore } }
        ?: emptyList()
}
