package com.simprints.feature.orchestrator.model

import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.responses.AppResponse

internal data class OrchestratorResult(
    val request: ActionRequest?,
    val response: AppResponse,
)
