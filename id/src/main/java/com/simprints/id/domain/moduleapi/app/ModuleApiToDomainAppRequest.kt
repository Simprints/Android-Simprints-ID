package com.simprints.id.domain.moduleapi.app

import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.*
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppConfirmIdentityRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppEnrolLastBiometricsRequest
import com.simprints.moduleapi.app.requests.*

fun IAppRequest.fromModuleApiToDomain(): AppRequest =
    when (this) {
        is IAppEnrolRequest ->
            AppEnrolRequest(projectId, userId, moduleId, metadata)

        is IAppIdentifyRequest ->
            AppIdentifyRequest(projectId, userId, moduleId, metadata)

        is IAppVerifyRequest ->
            AppVerifyRequest(projectId, userId, moduleId, metadata, verifyGuid)

        is IAppConfirmIdentityRequest ->
            AppConfirmIdentityRequest(projectId, userId, sessionId, selectedGuid)

        is IAppEnrolLastBiometricsRequest ->
            AppEnrolLastBiometricsRequest(projectId, userId, moduleId, metadata, sessionId)

        else -> throw IllegalArgumentException("Request not recognised")
    }
