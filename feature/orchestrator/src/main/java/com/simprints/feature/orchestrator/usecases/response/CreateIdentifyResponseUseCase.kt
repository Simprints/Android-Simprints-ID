package com.simprints.feature.orchestrator.usecases.response

import com.simprints.core.domain.common.ModalitySdkType
import com.simprints.core.domain.response.AppMatchConfidence
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.getModalitySdkConfig
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.matching.MatchResult
import com.simprints.infra.orchestration.data.responses.AppIdentifyResponse
import com.simprints.infra.orchestration.data.responses.AppMatchResult
import com.simprints.infra.orchestration.data.responses.AppResponse
import java.io.Serializable
import javax.inject.Inject

internal class CreateIdentifyResponseUseCase @Inject constructor(
    private val eventRepository: SessionEventRepository,
) {
    suspend operator fun invoke(
        projectConfiguration: ProjectConfiguration,
        results: List<Serializable>,
    ): AppResponse {
        val isMultiFactorIdEnabled = projectConfiguration.multifactorId?.allowedExternalCredentials?.isNotEmpty() ?: false

        val currentSessionId = eventRepository.getCurrentSessionScope().id
        return AppIdentifyResponse(
            sessionId = currentSessionId,
            isMultiFactorIdEnabled = isMultiFactorIdEnabled,
            // Return the results with the highest confidence score
            identifications = getResults(results, projectConfiguration),
        )
    }

    /**
     * Combines all of the matching results per SDK and returns up to [maxNbOfReturnedCandidates] results from the SDK with
     * the highest overall score in descending order. Credential matches take precedence over direct matches.
     *
     * If there are any matches of [AppMatchConfidence.HIGH], only those will be returned,
     * otherwise everything above [AppMatchConfidence.NONE] is returned.
     */
    private fun getResults(
        results: List<Serializable>,
        projectConfiguration: ProjectConfiguration,
    ): List<AppMatchResult> {
        val credentialResultsDescending = mapCredentialSearchResultsPerSdk(results, projectConfiguration)
        val matchResultResultsDescending = mapMatchResultsPerSdk(results, projectConfiguration)

        return (credentialResultsDescending.keys + matchResultResultsDescending.keys)
            .associateWith { credentialResultsDescending[it].orEmpty() + matchResultResultsDescending[it].orEmpty() }
            .filterValues { it.isNotEmpty() }
            .maxByOrNull { (_, values) -> values.maxOfOrNull { it.confidenceScore } ?: 0 }
            ?.let { (_, results) ->
                val goodResults = results.filter { it.matchConfidence != AppMatchConfidence.NONE }
                // Attempt to include only high confidence matches
                goodResults
                    .filter { it.matchConfidence == AppMatchConfidence.HIGH }
                    .ifEmpty { goodResults }
                    .take(projectConfiguration.identification.maxNbOfReturnedCandidates)
                    .distinctBy(AppMatchResult::guid)
            }.orEmpty()
    }

    private fun mapCredentialSearchResultsPerSdk(
        results: List<Serializable>,
        projectConfiguration: ProjectConfiguration,
    ): Map<ModalitySdkType, List<AppMatchResult>> = results
        .filterIsInstance<ExternalCredentialSearchResult>()
        // Mapping the result to the common final type and pairing it with the sdk for later grouping
        .flatMap { credentialSearchResult ->
            credentialSearchResult.matchResults.mapNotNull { credentialMatchResult ->
                val sdk = credentialMatchResult.bioSdk
                val policy = projectConfiguration.getModalitySdkConfig(sdk)?.decisionPolicy ?: return@mapNotNull null
                val matchResult = credentialMatchResult.comparisonResult

                sdk to AppMatchResult(matchResult.subjectId, matchResult.comparisonScore, policy, true)
            }
        }.groupDescendingResultsBySdk()

    private fun mapMatchResultsPerSdk(
        results: List<Serializable>,
        projectConfiguration: ProjectConfiguration,
    ): Map<ModalitySdkType, List<AppMatchResult>> = results
        .filterIsInstance<MatchResult>()
        .flatMap { matchResult ->
            val policy = projectConfiguration
                .getModalitySdkConfig(matchResult.sdk)
                ?.decisionPolicy
                ?: return@flatMap emptyList()

            matchResult.results.map {
                matchResult.sdk to AppMatchResult(it.subjectId, it.comparisonScore, policy, false)
            }
        }.groupDescendingResultsBySdk()

    private fun List<Pair<ModalitySdkType, AppMatchResult>>.groupDescendingResultsBySdk() = groupBy(
        { (sdk, _) -> sdk },
        { (_, resultsPerSdk) -> resultsPerSdk },
    ).mapValues { (_, results) -> results.sortedByDescending { it.confidenceScore } }
}
