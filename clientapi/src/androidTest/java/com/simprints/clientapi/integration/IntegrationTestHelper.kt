package com.simprints.clientapi.integration

import com.simprints.moduleapi.app.requests.IAppEnrollRequest
import com.simprints.moduleapi.app.requests.IAppIdentifyRequest
import com.simprints.moduleapi.app.requests.IAppVerifyRequest
import com.simprints.moduleapi.app.requests.confirmations.IAppIdentityConfirmationRequest
import com.simprints.moduleapi.app.responses.*
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class AppConfirmationResponse(
    override val identificationOutcome: Boolean,
    override val type: IAppResponseType = IAppResponseType.CONFIRMATION
) : IAppConfirmationResponse

@Parcelize
internal data class AppEnrolResponse(
    override val guid: String,
    override val type: IAppResponseType = IAppResponseType.ENROL
) : IAppEnrolResponse

@Parcelize
internal data class AppVerifyResponse(
    override val matchResult: AppMatchResult,
    override val type: IAppResponseType = IAppResponseType.VERIFY
) : IAppVerifyResponse

@Parcelize
internal data class AppMatchResult(
    override val guid: String,
    override val confidence: Int,
    override val tier: IAppResponseTier
) : IAppMatchResult

@Parcelize
internal data class AppRefusalResponse(
    override val reason: String,
    override val extra: String,
    override val type: IAppResponseType = IAppResponseType.REFUSAL
) : IAppRefusalFormResponse

@Parcelize
internal data class AppIdentifyResponse(
    override val identifications: List<IAppMatchResult>,
    override val sessionId: String,
    override val type: IAppResponseType = IAppResponseType.IDENTIFY
) : IAppIdentifyResponse


@Parcelize
internal data class AppErrorResponse(
    override val reason: IAppErrorReason,
    override val type: IAppResponseType = IAppResponseType.ERROR
) : IAppErrorResponse

@Parcelize
internal data class AppEnrollRequest(
    override val projectId: String,
    override val userId: String,
    override val moduleId: String,
    override val metadata: String
) : IAppEnrollRequest

@Parcelize
internal data class AppIdentifyRequest(
    override val projectId: String,
    override val userId: String,
    override val moduleId: String,
    override val metadata: String
) : IAppIdentifyRequest

@Parcelize
internal data class AppVerifyRequest(
    override val projectId: String,
    override val userId: String,
    override val moduleId: String,
    override val metadata: String,
    override val verifyGuid: String
) : IAppVerifyRequest

@Parcelize
internal data class AppIdentityConfirmationRequest(
    override val projectId: String,
    override val sessionId: String,
    override val selectedGuid: String
) : IAppIdentityConfirmationRequest
