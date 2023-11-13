package com.simprints.feature.orchestrator.model.responses

import com.simprints.infra.orchestration.moduleapi.app.responses.IAppMatchResult
import com.simprints.infra.orchestration.moduleapi.app.responses.IAppResponseType
import com.simprints.infra.orchestration.moduleapi.app.responses.IAppVerifyResponse
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class AppVerifyResponse(
    override val matchResult: IAppMatchResult,
    override val type: IAppResponseType = IAppResponseType.VERIFY
) : IAppVerifyResponse


