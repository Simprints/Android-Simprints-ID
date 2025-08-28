package com.simprints.feature.orchestrator.usecases.response

import com.simprints.infra.config.store.models.ModalitySdkConfigurationMapping
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.getModalityConfigs
import com.simprints.matcher.MatchResult
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
        if (!projectConfiguration.general.duplicateBiometricEnrolmentCheck) {
            // Duplicate check on enrolment is disabled
            return true
        }

        val configs = projectConfiguration.getModalityConfigs()
        val matchResults = results.mapNotNull { it as? MatchResult }
        return matchResults
            .map { result -> isValidEnrolmentResult(configs, result) }
            .all { it }
    }

    // Missing results and configuration are ignored as "valid" to allow creating new records.
    private fun isValidEnrolmentResult(
        configs: ModalitySdkConfigurationMapping,
        result: MatchResult?,
    ): Boolean = result?.let {
        configs[result.modality]
            ?.get(result.bioSdk)
            ?.decisionPolicy
            ?.medium
            ?.toFloat()
            ?.let { threshold -> result.results.all { it.confidence < threshold } }
    } != false
}
