package com.simprints.feature.orchestrator.usecases.response

import com.simprints.core.domain.response.AppErrorReason
import com.simprints.feature.selectsubject.SelectSubjectResult
import com.simprints.infra.orchestration.data.responses.AppConfirmationResponse
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.responses.AppResponse
import java.io.Serializable
import javax.inject.Inject

internal class CreateConfirmIdentityResponseUseCase @Inject constructor() {
    operator fun invoke(results: List<Serializable>): AppResponse = results
        .filterIsInstance(SelectSubjectResult::class.java)
        .lastOrNull()
        ?.let { AppConfirmationResponse(true) }
        ?: AppErrorResponse(AppErrorReason.UNEXPECTED_ERROR)
}
