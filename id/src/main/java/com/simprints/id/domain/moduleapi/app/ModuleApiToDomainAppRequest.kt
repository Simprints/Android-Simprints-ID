package com.simprints.id.domain.moduleapi.app

import com.simprints.core.domain.tokenization.asTokenizedRaw
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.AppVerifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppConfirmIdentityRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppEnrolLastBiometricsRequest
import com.simprints.moduleapi.app.requests.IAppConfirmIdentityRequest
import com.simprints.moduleapi.app.requests.IAppEnrolLastBiometricsRequest
import com.simprints.moduleapi.app.requests.IAppEnrolRequest
import com.simprints.moduleapi.app.requests.IAppIdentifyRequest
import com.simprints.moduleapi.app.requests.IAppRequest
import com.simprints.moduleapi.app.requests.IAppVerifyRequest

fun IAppRequest.fromModuleApiToDomain(): AppRequest =
    when (this) {
        is IAppEnrolRequest ->
            AppEnrolRequest(projectId, userId.asTokenizedRaw(), moduleId, metadata)

        is IAppIdentifyRequest ->
            AppIdentifyRequest(projectId, userId.asTokenizedRaw(), moduleId, metadata)

        is IAppVerifyRequest ->
            AppVerifyRequest(projectId, userId.asTokenizedRaw(), moduleId, metadata, verifyGuid)

        is IAppConfirmIdentityRequest ->
            AppConfirmIdentityRequest(projectId, userId.asTokenizedRaw(), sessionId, selectedGuid)

        is IAppEnrolLastBiometricsRequest ->
            AppEnrolLastBiometricsRequest(projectId, userId.asTokenizedRaw(), moduleId, metadata, sessionId)

        else -> throw IllegalArgumentException("Request not recognised")
    }
