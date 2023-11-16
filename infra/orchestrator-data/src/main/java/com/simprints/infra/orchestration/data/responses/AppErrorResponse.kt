package com.simprints.infra.orchestration.data.responses

import com.simprints.core.domain.response.AppErrorReason
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppErrorResponse(
    val reason: AppErrorReason,
) : AppResponse()
