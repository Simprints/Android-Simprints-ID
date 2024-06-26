package com.simprints.feature.orchestrator.usecases.response

import com.simprints.core.domain.response.AppErrorReason
import com.simprints.infra.config.store.models.ProjectConfiguration
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
        ?: AppErrorResponse(AppErrorReason.UNEXPECTED_ERROR)

    private fun getFingerprintMatchResults(
        projectConfiguration: ProjectConfiguration,
        results: List<Serializable>,
    ) =  results.filterIsInstance<FingerprintMatchResult>()
            .lastOrNull()?.let { fingerprintMatchResult ->
                projectConfiguration.fingerprint
                    ?.getSdkConfiguration(fingerprintMatchResult.sdk)
                    ?.decisionPolicy
                    ?.let { decisionPolicy ->
                        fingerprintMatchResult.results
                            .maxByOrNull { it.confidence }
                            ?.let { AppMatchResult(it.subjectId, it.confidence, decisionPolicy) }
                    }
            }

    private fun getFaceMatchResults(
        projectConfiguration: ProjectConfiguration,
        results: List<Serializable>,
    ) = results.filterIsInstance<FaceMatchResult>()
        .lastOrNull()?.let { faceMatchResult ->
            projectConfiguration.face?.decisionPolicy?.let { decisionPolicy ->
                faceMatchResult.results
                    .maxByOrNull { it.confidence }
                    ?.let { AppMatchResult(it.subjectId, it.confidence, decisionPolicy) }
            }
        }
}
