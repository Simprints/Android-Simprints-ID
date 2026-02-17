package com.simprints.feature.orchestrator.usecases.response

import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.matching.FaceMatchResult
import com.simprints.infra.matching.FingerprintMatchResult
import com.simprints.infra.matching.MatchResultItem
import com.simprints.infra.orchestration.data.responses.AppExternalCredential
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

        val faceMatchResults = getFaceMatchResults(results, projectConfiguration)
        val bestFaceConfidence = faceMatchResults.firstOrNull()?.confidenceScore ?: 0

        val fingerprintMatchResults = getFingerprintResults(results, projectConfiguration)
        val bestFingerprintConfidence = fingerprintMatchResults.firstOrNull()?.confidenceScore ?: 0

        val isUsingFingerprintResults = bestFingerprintConfidence > bestFaceConfidence
        val bestMatcherIdentifications = if (isUsingFingerprintResults) {
            fingerprintMatchResults
        } else {
            faceMatchResults
        }
        val allCredentialResults = (
            credentialResultsMapper(results, projectConfiguration, isFace = true) +
                credentialResultsMapper(results, projectConfiguration, isFace = false)
        ).sortedByDescending(AppMatchResult::confidenceScore)

        // Return the results with the credential results on top, followed by highest confidence score 1:N match results
        val identifications = (allCredentialResults + bestMatcherIdentifications)
            .distinctBy(AppMatchResult::guid)
            .take(projectConfiguration.identification.maxNbOfReturnedCandidates)

        val externalCredential = results
            .filterIsInstance(ExternalCredentialSearchResult::class.java)
            .lastOrNull()
            ?.scannedCredential
            .toAppExternalCredential()

        return AppIdentifyResponse(
            sessionId = currentSessionId,
            isMultiFactorIdEnabled = isMultiFactorIdEnabled,
            identifications = identifications,
            scannedCredential = externalCredential,
        )
    }

    private fun ScannedCredential?.toAppExternalCredential(): AppExternalCredential? = this?.let { scannedCredential ->
        AppExternalCredential(
            id = scannedCredential.credentialScanId,
            value = scannedCredential.scannedValue,
            type = scannedCredential.credentialType,
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
                fingerprintMatchResult.results.mapToMatchResults(
                    decisionPolicy = fingerprintDecisionPolicy,
                    isCredentialMatch = false,
                    verificationMatchThreshold = null,
                )
            }
    } ?: emptyList()

    private fun getFaceMatchResults(
        results: List<Serializable>,
        projectConfiguration: ProjectConfiguration,
    ) = results.filterIsInstance<FaceMatchResult>().lastOrNull()?.let { faceMatchResult ->
        projectConfiguration.face
            ?.getSdkConfiguration(faceMatchResult.sdk)
            ?.decisionPolicy
            ?.let { faceDecisionPolicy ->
                faceMatchResult.results.mapToMatchResults(
                    decisionPolicy = faceDecisionPolicy,
                    isCredentialMatch = false,
                    verificationMatchThreshold = null,
                )
            }
    } ?: emptyList()

    private fun List<MatchResultItem>.mapToMatchResults(
        decisionPolicy: DecisionPolicy,
        verificationMatchThreshold: Float?,
        isCredentialMatch: Boolean,
    ): List<AppMatchResult> {
        val results = if (isCredentialMatch) {
            // Credential matches are returned regardless of confidence score
            this
        } else {
            // Attempt to include only high confidence matches.
            this
                .filter { it.confidence >= decisionPolicy.low }
                .sortedByDescending { it.confidence }
                .let { goodResults ->
                    goodResults
                        .filter { it.confidence >= decisionPolicy.high }
                        .ifEmpty { goodResults }
                }
        }
        return results
            .map {
                AppMatchResult(
                    guid = it.subjectId,
                    confidenceScore = it.confidence,
                    decisionPolicy = decisionPolicy,
                    isCredentialMatch = isCredentialMatch,
                    verificationMatchThreshold = verificationMatchThreshold,
                )
            }
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
            val credentialMatchItems = credentialSearchResult.matchResults.map { it.matchResult }
            val faceMatchItems = credentialMatchItems.filterIsInstance<FaceMatchResult.Item>()
            val fingerMatchItems = credentialMatchItems.filterIsInstance<FingerprintMatchResult.Item>()
            val (decisionPolicy, verificationMatchThreshold) = if (isFace) {
                credentialSearchResult.matchResults.find { it.faceBioSdk != null }?.faceBioSdk?.let { sdk ->
                    val config = projectConfiguration.face?.getSdkConfiguration(sdk)
                    config?.decisionPolicy to config?.verificationMatchThreshold
                }
            } else {
                credentialSearchResult.matchResults.find { it.fingerprintBioSdk != null }?.fingerprintBioSdk?.let { sdk ->
                    val config = projectConfiguration.fingerprint?.getSdkConfiguration(sdk)
                    config?.decisionPolicy to config?.verificationMatchThreshold
                }
            } ?: (null to null)

            if (decisionPolicy == null) return@let emptyList()
            val matches = if (isFace) faceMatchItems else fingerMatchItems
            return@let matches
                .mapToMatchResults(
                    decisionPolicy = decisionPolicy,
                    isCredentialMatch = true,
                    verificationMatchThreshold = verificationMatchThreshold,
                )
        }.orEmpty()
}
