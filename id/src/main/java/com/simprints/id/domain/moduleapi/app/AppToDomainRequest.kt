package com.simprints.id.domain.moduleapi.app
import com.simprints.id.domain.moduleapi.app.requests.*
import com.simprints.moduleapi.app.requests.IAppEnrollRequest
import com.simprints.moduleapi.app.requests.IAppIdentifyRequest
import com.simprints.moduleapi.app.requests.IAppRequest
import com.simprints.moduleapi.app.requests.IAppVerifyRequest
import com.simprints.moduleapi.app.requests.confirmations.IAppIdentifyConfirmation

object AppRequestToDomainRequest {

    fun fromAppToDomainRequest(appRequest: IAppRequest): AppBaseRequest =
        when (appRequest) {
            is IAppEnrollRequest -> AppEnrolRequest(appRequest)
            is IAppVerifyRequest -> AppVerifyRequest(appRequest)
            is IAppIdentifyRequest -> AppIdentifyRequest(appRequest)
            is IAppIdentifyConfirmation -> AppIdentityConfirmationRequest(appRequest)
            else -> throw IllegalArgumentException("Invalid app request") //StopShip
        }
}
