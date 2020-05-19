package com.simprints.id.domain.moduleapi.app

import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.moduleapi.app.requests.*

fun IAppRequest.fromModuleApiToDomain(): AppRequest =
    when (this) {
        is IAppEnrolRequest ->
            AppRequest.AppRequestFlow.AppEnrolRequest(projectId, userId, moduleId, metadata)

        is IAppIdentifyRequest ->
            AppRequest.AppRequestFlow.AppIdentifyRequest(projectId, userId, moduleId, metadata)

        is IAppVerifyRequest ->
            AppRequest.AppRequestFlow.AppVerifyRequest(projectId, userId, moduleId, metadata, verifyGuid)

        is IAppConfirmIdentityRequest ->
            AppRequest.AppConfirmIdentityRequest(projectId, userId, sessionId, selectedGuid)

        is IAppEnrolLastBiometricsRequest ->
            AppRequest.AppEnrolLastBiometricsRequest(projectId, userId, moduleId, metadata, sessionId)

        else -> throw IllegalArgumentException("Request not recognised")
    }
