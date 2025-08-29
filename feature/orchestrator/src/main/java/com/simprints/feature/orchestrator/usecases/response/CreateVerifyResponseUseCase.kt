package com.simprints.feature.orchestrator.usecases.response

import com.simprints.core.domain.response.AppErrorReason
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.getModalityConfigs
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FINGER_MATCHING
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.responses.AppMatchResult
import com.simprints.infra.orchestration.data.responses.AppResponse
import com.simprints.infra.orchestration.data.responses.AppVerifyResponse
import com.simprints.matcher.MatchResult
import java.io.Serializable
import javax.inject.Inject

internal class CreateVerifyResponseUseCase @Inject constructor() {
    operator fun invoke(
        projectConfiguration: ProjectConfiguration,
        results: List<Serializable>,
    ): AppResponse {
        val modalityConfigs = projectConfiguration.getModalityConfigs()

        return results
            .filterIsInstance<MatchResult>()
            .mapNotNull { matchResult ->
                modalityConfigs[matchResult.modality]?.get(matchResult.bioSdk)?.let { sdkConfiguration ->
                    matchResult.results.maxByOrNull { it.confidence }?.let {
                        AppMatchResult(
                            guid = it.subjectId,
                            confidenceScore = it.confidence,
                            decisionPolicy = sdkConfiguration.decisionPolicy,
                            verificationMatchThreshold = sdkConfiguration.verificationMatchThreshold,
                        )
                    }
                }
            }.maxByOrNull { it.confidenceScore }
            ?.let { AppVerifyResponse(it) }
            ?: AppErrorResponse(AppErrorReason.UNEXPECTED_ERROR).also {
                // if subject enrolled with an SDK and the user tries to verify with another SDK
                Simber.i("No match results found", tag = FINGER_MATCHING)
            }
    }
}
