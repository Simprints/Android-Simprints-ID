package com.simprints.feature.orchestrator.model.responses

import com.simprints.core.domain.response.AppErrorReason
import com.simprints.infra.orchestration.moduleapi.app.responses.IAppErrorResponse
import com.simprints.infra.orchestration.moduleapi.app.responses.IAppResponseType
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class AppErrorResponse(
    override val reason: AppErrorReason,
    override val type: IAppResponseType = IAppResponseType.ERROR
) : IAppErrorResponse
