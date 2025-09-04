package com.simprints.feature.enrollast.screen.usecase

import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.screen.EnrolLastState
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.getModalitySdkConfig
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ENROLMENT
import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class CheckForDuplicateEnrolmentsUseCase @Inject constructor() {
    operator fun invoke(
        projectConfig: ProjectConfiguration,
        steps: List<EnrolLastBiometricStepResult>,
    ): EnrolLastState.ErrorType? {
        if (!projectConfig.general.duplicateBiometricEnrolmentCheck) return null
        val matchResults = getFingerprintMatchResult(steps)

        return when {
            matchResults.isEmpty() -> {
                Simber.e(
                    "No match response. Must be either fingerprint, face or both",
                    MissingMatchResultException(),
                    tag = ENROLMENT,
                )
                EnrolLastState.ErrorType.NO_MATCH_RESULTS
            }

            isAnyResponseWithHighConfidence(projectConfig, matchResults) -> {
                Simber.i("There is a subject with confidence score above the high confidence level", tag = ENROLMENT)
                EnrolLastState.ErrorType.DUPLICATE_ENROLMENTS
            }

            else -> null
        }
    }

    private fun getFingerprintMatchResult(steps: List<EnrolLastBiometricStepResult>) = steps
        .filterIsInstance<EnrolLastBiometricStepResult.MatchResult>()

    private fun isAnyResponseWithHighConfidence(
        configuration: ProjectConfiguration,
        responses: List<EnrolLastBiometricStepResult.MatchResult>,
    ): Boolean = responses.any { result ->
        val threshold = configuration
            .getModalitySdkConfig(result.sdk)
            ?.decisionPolicy
            ?.high
            ?.toFloat() ?: Float.MAX_VALUE

        result.results.any { it.confidence >= threshold }
    }

    private class MissingMatchResultException : IllegalStateException("No match response in duplicate check.")
}
