package com.simprints.feature.orchestrator.usecases.response

import com.simprints.core.domain.response.AppErrorReason
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.getModalitySdkConfig
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FINGER_MATCHING
import com.simprints.infra.logging.Simber
import com.simprints.infra.matching.MatchResult
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.responses.AppMatchResult
import com.simprints.infra.orchestration.data.responses.AppResponse
import com.simprints.infra.orchestration.data.responses.AppVerifyResponse
import java.io.Serializable
import javax.inject.Inject

internal class CreateVerifyResponseUseCase @Inject constructor() {
    operator fun invoke(
        projectConfiguration: ProjectConfiguration,
        results: List<Serializable>,
    ): AppResponse = getMatchResults(projectConfiguration, results)
        .maxByOrNull { it.confidenceScore }
        ?.let { AppVerifyResponse(it) }
        ?: AppErrorResponse(AppErrorReason.UNEXPECTED_ERROR).also {
            // if subject enrolled with an SDK and the user tries to verify with another SDK
            Simber.i("No match results found", tag = FINGER_MATCHING)
        }

    private fun getMatchResults(
        projectConfiguration: ProjectConfiguration,
        results: List<Serializable>,
    ) = results
        .filterIsInstance<MatchResult>()
        .mapNotNull { matchResult ->
            projectConfiguration.getModalitySdkConfig(matchResult.sdk)?.let { sdkConfiguration ->
                matchResult.results.maxByOrNull { it.confidence }?.let {
                    AppMatchResult(
                        guid = it.subjectId,
                        confidenceScore = it.confidence,
                        decisionPolicy = sdkConfiguration.decisionPolicy,
                        verificationMatchThreshold = sdkConfiguration.verificationMatchThreshold,
                        isCredentialMatch = false,
                    )
                }
            }
        }
}
