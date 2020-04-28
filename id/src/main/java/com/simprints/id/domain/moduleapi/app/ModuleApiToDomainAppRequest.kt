package com.simprints.id.domain.moduleapi.app

import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.fromModuleApiToDomain
import com.simprints.moduleapi.app.requests.IAppRequest

object AppRequestToDomainRequest {

    fun fromAppToDomainRequest(appRequest: IAppRequest): AppRequest =
        appRequest.fromModuleApiToDomain()
}
