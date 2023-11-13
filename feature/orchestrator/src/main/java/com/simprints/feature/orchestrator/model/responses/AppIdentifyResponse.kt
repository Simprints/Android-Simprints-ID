package com.simprints.feature.orchestrator.model.responses

import com.simprints.infra.orchestration.moduleapi.app.responses.IAppIdentifyResponse
import com.simprints.infra.orchestration.moduleapi.app.responses.IAppMatchResult
import com.simprints.infra.orchestration.moduleapi.app.responses.IAppResponseType
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class AppIdentifyResponse(
    override val identifications: List<IAppMatchResult>,
    override val sessionId: String,
    override val type: IAppResponseType = IAppResponseType.IDENTIFY
) : IAppIdentifyResponse
