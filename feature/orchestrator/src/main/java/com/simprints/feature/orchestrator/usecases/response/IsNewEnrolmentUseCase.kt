package com.simprints.feature.orchestrator.usecases.response

import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.getModalitySdkConfig
import com.simprints.infra.matching.MatchResult
import java.io.Serializable
import javax.inject.Inject

internal class IsNewEnrolmentUseCase @Inject constructor() {
    /**
     * Returns true if the result is an actual enrollment and false if there is
     * a duplicate record and the result should be returned as an identification instead.
     */
    operator fun invoke(
        projectConfiguration: ProjectConfiguration,
        results: List<Serializable>,
    ): Boolean {
        val hasCredentialMatchResults =
            results
                .filterIsInstance<ExternalCredentialSearchResult>()
                .firstOrNull()
                ?.matchResults
                ?.isNotEmpty() ?: false
        if (hasCredentialMatchResults) {
            return false
        }

        if (!projectConfiguration.general.duplicateBiometricEnrolmentCheck) {
            // Duplicate check on enrolment is disabled
            return true
        }

        val mathcResults = results
            .filterIsInstance<MatchResult>()
            .takeUnless { it.isEmpty() } // Missing match results is "valid" to allow creating new records.
            ?: return true

        return mathcResults.all { isResultBelowMediumThreshold(projectConfiguration, it) }
    }

    // Missing configuration are ignored as "valid" to allow creating new records.
    private fun isResultBelowMediumThreshold(
        projectConfiguration: ProjectConfiguration,
        matchResult: MatchResult,
    ): Boolean = projectConfiguration
        .getModalitySdkConfig(matchResult.sdk)
        ?.decisionPolicy
        ?.medium
        ?.toFloat()
        ?.let { threshold -> matchResult.results.isNotEmpty() && matchResult.results.all { it.comparisonScore < threshold } } != false
}
