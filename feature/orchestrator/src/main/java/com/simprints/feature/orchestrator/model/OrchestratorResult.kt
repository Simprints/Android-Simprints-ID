package com.simprints.feature.orchestrator.model

import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.moduleapi.app.responses.IAppResponse

internal data class OrchestratorResult(
    val request: ActionRequest?,
    val response: IAppResponse
)
