package com.simprints.id.domain.moduleapi.app

import com.simprints.core.domain.tokenization.asTokenizableRaw
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
            AppEnrolRequest(
                projectId = projectId,
                userId = userId.asTokenizableRaw(),
                moduleId = moduleId.asTokenizableRaw(),
                metadata = metadata
            )

        is IAppIdentifyRequest ->
            AppIdentifyRequest(
                projectId = projectId,
                userId = userId.asTokenizableRaw(),
                moduleId = moduleId.asTokenizableRaw(),
                metadata = metadata
            )

        is IAppVerifyRequest ->
            AppVerifyRequest(
                projectId = projectId,
                userId = userId.asTokenizableRaw(),
                moduleId = moduleId.asTokenizableRaw(),
                metadata = metadata,
                verifyGuid = verifyGuid
            )

        is IAppConfirmIdentityRequest ->
            AppConfirmIdentityRequest(
                projectId = projectId,
                userId = userId.asTokenizableRaw(),
                sessionId = sessionId,
                selectedGuid = selectedGuid
            )

        is IAppEnrolLastBiometricsRequest ->
            AppEnrolLastBiometricsRequest(
                projectId = projectId,
                userId = userId.asTokenizableRaw(),
                moduleId = moduleId.asTokenizableRaw(),
                metadata = metadata,
                identificationSessionId = sessionId
            )

        else -> throw IllegalArgumentException("Request not recognised")
    }
