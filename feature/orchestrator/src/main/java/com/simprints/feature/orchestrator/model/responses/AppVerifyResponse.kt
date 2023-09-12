package com.simprints.feature.orchestrator.model.responses

import com.simprints.moduleapi.app.responses.IAppMatchResult
import com.simprints.moduleapi.app.responses.IAppResponseType
import com.simprints.moduleapi.app.responses.IAppVerifyResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppVerifyResponse(
    override val matchResult: IAppMatchResult,
    override val type: IAppResponseType = IAppResponseType.VERIFY
) : IAppVerifyResponse


