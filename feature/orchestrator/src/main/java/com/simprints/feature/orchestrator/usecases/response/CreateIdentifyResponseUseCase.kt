package com.simprints.feature.orchestrator.usecases.response

import com.simprints.core.domain.sample.MatchConfidence
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
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
        val credentialFaceMatchResults = credentialResultsMapper(results, projectConfiguration, isFace = true)
        val credentialFingerprintMatchResults = credentialResultsMapper(results, projectConfiguration, isFace = false)

        val currentSessionId = eventRepository.getCurrentSessionScope().id

        val faceResults = credentialFaceMatchResults + getFaceMatchResults(results, projectConfiguration)
        val bestFaceConfidence = faceResults.firstOrNull()?.confidenceScore ?: 0

        val fingerprintResults = credentialFingerprintMatchResults + getFingerprintResults(results, projectConfiguration)
        val bestFingerprintConfidence = fingerprintResults.firstOrNull()?.confidenceScore ?: 0

        return AppIdentifyResponse(
            sessionId = currentSessionId,
            // Return the results with the highest confidence score
            identifications = if (bestFingerprintConfidence > bestFaceConfidence) {
                fingerprintResults.distinctBy(AppMatchResult::guid)
            } else {
                faceResults.distinctBy(AppMatchResult::guid)
            },
        )
    }

    private fun getFingerprintResults(
        results: List<Serializable>,
        projectConfiguration: ProjectConfiguration,
    ) = results
        .filterIsInstance<MatchResult>()
        .lastOrNull { it.sdk is FingerprintConfiguration.BioSdk }
        ?.let { fingerprintMatchResult ->
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
    ) = results
        .filterIsInstance<MatchResult>()
        .lastOrNull { it.sdk is FaceConfiguration.BioSdk }
        ?.let { faceMatchResult ->
            projectConfiguration.face
                ?.getSdkConfiguration(faceMatchResult.sdk)
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

    private fun List<MatchConfidence>.mapToMatchResults(
        decisionPolicy: DecisionPolicy,
        projectConfiguration: ProjectConfiguration,
    ): List<AppMatchResult> {
        val goodResults = this
            .filter { it.confidence >= decisionPolicy.low }
            .sortedByDescending { it.confidence }
        // Attempt to include only high confidence matches
        return goodResults
            .filter { it.confidence >= decisionPolicy.high }
            .ifEmpty { goodResults }
            .take(projectConfiguration.identification.maxNbOfReturnedCandidates)
            .map { AppMatchResult(it.subjectId, it.confidence, decisionPolicy) }
    }

    /**
     * Checks if any of the [results] items is an instance of [ExternalCredentialSearchResult]. If such item is found, then returns a list
     * of candidates whose verification matching score  between the taken biometric probe, and a biometric probe linked to is above
     * project's verification threshold.
     *
     * @return list of [AppMatchResult] containing possible verification matches
     */
    private fun credentialResultsMapper(
        results: List<Serializable>,
        projectConfiguration: ProjectConfiguration,
        isFace: Boolean,
    ) = results
        .filterIsInstance<ExternalCredentialSearchResult>()
        .firstOrNull()
        ?.let { credentialSearchResult ->
            val faceMatchItems = credentialSearchResult.matchResults.filter { it.faceBioSdk != null }.map { it.matchResult }
            val fingerMatchItems = credentialSearchResult.matchResults.filter { it.fingerprintBioSdk != null }.map { it.matchResult }
            val decisionPolicy = if (isFace) {
                credentialSearchResult.matchResults.find { it.faceBioSdk != null }?.faceBioSdk?.let { sdk ->
                    projectConfiguration.face
                        ?.getSdkConfiguration(sdk)
                        ?.decisionPolicy
                }
            } else {
                credentialSearchResult.matchResults.find { it.fingerprintBioSdk != null }?.fingerprintBioSdk?.let { sdk ->
                    projectConfiguration.fingerprint
                        ?.getSdkConfiguration(sdk)
                        ?.decisionPolicy
                }
            }
            if (decisionPolicy == null) return@let emptyList()
            val matches = if (isFace) faceMatchItems else fingerMatchItems
            return@let matches
                .mapToMatchResults(decisionPolicy, projectConfiguration)
                .sortedByDescending(AppMatchResult::confidenceScore)
        }.orEmpty()
}
