package com.simprints.feature.orchestrator.usecases.response

import android.os.Parcelable
import com.simprints.matcher.FaceMatchResult
import com.simprints.feature.orchestrator.model.responses.AppErrorResponse
import com.simprints.feature.orchestrator.model.responses.AppMatchResult
import com.simprints.feature.orchestrator.model.responses.AppVerifyResponse
import com.simprints.infra.config.domain.models.DecisionPolicy
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.moduleapi.app.responses.IAppErrorReason
import com.simprints.moduleapi.app.responses.IAppResponse
import javax.inject.Inject

internal class CreateVerifyResponseUseCase @Inject constructor() {

    operator fun invoke(
        projectConfiguration: ProjectConfiguration,
        results: List<Parcelable>,
    ): IAppResponse = listOfNotNull(
        // TODO fingerprint match results
        getFaceMatchResults(projectConfiguration.face?.decisionPolicy, results),
    ).maxByOrNull { it.confidenceScore }
        ?.let { AppVerifyResponse(it) }
        ?: AppErrorResponse(IAppErrorReason.UNEXPECTED_ERROR)

    private fun getFaceMatchResults(
        faceDecisionPolicy: DecisionPolicy?,
        results: List<Parcelable>,
    ) = if (faceDecisionPolicy != null) {
        results.filterIsInstance(FaceMatchResult::class.java)
            .lastOrNull()
            ?.results
            ?.maxByOrNull { it.confidence }
            ?.let { AppMatchResult(it.subjectId, it.confidence, faceDecisionPolicy) }
    } else null
}