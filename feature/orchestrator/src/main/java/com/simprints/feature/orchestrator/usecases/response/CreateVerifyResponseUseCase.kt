package com.simprints.feature.orchestrator.usecases.response

import com.simprints.core.domain.response.AppErrorReason
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.responses.AppMatchResult
import com.simprints.infra.orchestration.data.responses.AppResponse
import com.simprints.infra.orchestration.data.responses.AppVerifyResponse
import com.simprints.matcher.FaceMatchResult
import com.simprints.matcher.FingerprintMatchResult
import java.io.Serializable
import javax.inject.Inject

internal class CreateVerifyResponseUseCase @Inject constructor() {

    operator fun invoke(
        projectConfiguration: ProjectConfiguration,
        results: List<Serializable>,
    ): AppResponse = listOfNotNull(
        getFingerprintMatchResults(projectConfiguration, results),
        getFaceMatchResults(projectConfiguration, results),
    ).maxByOrNull { it.confidenceScore }
        ?.let { AppVerifyResponse(it) }
        ?: AppErrorResponse(AppErrorReason.UNEXPECTED_ERROR).also {
            //if subject enrolled with an SDK and the user tries to verify with another SDK
            Simber.tag(CrashReportTag.MATCHING.name).e("No match results found")
        }

    private fun getFingerprintMatchResults(
        projectConfiguration: ProjectConfiguration,
        results: List<Serializable>,
    ) =  results.filterIsInstance<FingerprintMatchResult>()
            .lastOrNull()?.let { fingerprintMatchResult ->
                projectConfiguration.fingerprint
                    ?.getSdkConfiguration(fingerprintMatchResult.sdk)
                    ?.let { sdkConfiguration ->
                        fingerprintMatchResult.results
                            .maxByOrNull { it.confidence }
                            ?.let {
                                AppMatchResult(
                                    guid = it.subjectId,
                                    confidenceScore = it.confidence,
                                    decisionPolicy = sdkConfiguration.decisionPolicy,
                                    verificationMatchThreshold = sdkConfiguration.verificationMatchThreshold
                                )
                            }
                    }
            }

    private fun getFaceMatchResults(
        projectConfiguration: ProjectConfiguration,
        results: List<Serializable>,
    ) = results.filterIsInstance<FaceMatchResult>()
        .lastOrNull()?.let { faceMatchResult ->
            projectConfiguration.face?.let { faceConfiguration ->
                faceMatchResult.results
                    .maxByOrNull { it.confidence }
                    ?.let {
                        AppMatchResult(
                            guid = it.subjectId,
                            confidenceScore = it.confidence,
                            decisionPolicy = faceConfiguration.decisionPolicy,
                            verificationMatchThreshold = faceConfiguration.verificationMatchThreshold
                        )
                    }
            }
        }
}
