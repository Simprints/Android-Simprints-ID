package com.simprints.id.domain.moduleapi.app
import com.simprints.id.domain.moduleapi.app.requests.AppBaseRequest
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.moduleapi.app.requests.IAppEnrollRequest
import com.simprints.moduleapi.app.requests.IAppIdentifyRequest
import com.simprints.moduleapi.app.requests.IAppRequest
import com.simprints.moduleapi.app.requests.IAppVerifyRequest

object AppRequestToDomainRequest {

    fun fromAppToDomainRequest(appRequest: IAppRequest): AppBaseRequest =
        when (appRequest) {
            is IAppEnrollRequest -> AppEnrolRequest(appRequest)
            is IAppVerifyRequest -> AppVerifyRequest(appRequest)
            is IAppIdentifyRequest -> AppIdentifyRequest(appRequest)
            else -> throw InvalidAppRequest()
        }
}
