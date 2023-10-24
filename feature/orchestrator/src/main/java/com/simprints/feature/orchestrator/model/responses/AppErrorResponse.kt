package com.simprints.feature.orchestrator.model.responses

import com.simprints.moduleapi.app.responses.IAppErrorReason
import com.simprints.moduleapi.app.responses.IAppErrorResponse
import com.simprints.moduleapi.app.responses.IAppResponseType
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppErrorResponse(
    override val reason: IAppErrorReason,
    override val type: IAppResponseType = IAppResponseType.ERROR
) : IAppErrorResponse
