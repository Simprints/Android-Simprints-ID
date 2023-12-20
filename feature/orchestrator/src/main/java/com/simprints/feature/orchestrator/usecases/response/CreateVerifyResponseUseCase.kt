package com.simprints.feature.orchestrator.usecases.response

import com.simprints.core.domain.response.AppErrorReason
import com.simprints.infra.config.store.models.DecisionPolicy
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
        getFingerprintMatchResults(
            projectConfiguration.fingerprint?.bioSdkConfiguration?.decisionPolicy,
            results
        ),
        getFaceMatchResults(projectConfiguration.face?.decisionPolicy, results),
    ).maxByOrNull { it.confidenceScore }
        ?.let { AppVerifyResponse(it) }
        ?: AppErrorResponse(AppErrorReason.UNEXPECTED_ERROR)

    private fun getFingerprintMatchResults(
        faceDecisionPolicy: DecisionPolicy?,
        results: List<Serializable>,
    ) = if (faceDecisionPolicy != null) {
        results.filterIsInstance(FingerprintMatchResult::class.java)
            .lastOrNull()
            ?.results
            ?.maxByOrNull { it.confidence }
            ?.let { AppMatchResult(it.subjectId, it.confidence, faceDecisionPolicy) }
    } else null

    private fun getFaceMatchResults(
        faceDecisionPolicy: DecisionPolicy?,
        results: List<Serializable>,
    ) = if (faceDecisionPolicy != null) {
        results.filterIsInstance(FaceMatchResult::class.java)
            .lastOrNull()
            ?.results
            ?.maxByOrNull { it.confidence }
            ?.let { AppMatchResult(it.subjectId, it.confidence, faceDecisionPolicy) }
    } else null
}
