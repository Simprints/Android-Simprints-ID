package com.simprints.infra.orchestration.moduleapi.app.responses

import com.simprints.core.domain.response.AppErrorReason

interface IAppErrorResponse : IAppResponse {
    val reason: AppErrorReason
}
