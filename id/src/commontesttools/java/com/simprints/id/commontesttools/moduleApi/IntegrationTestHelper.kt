package com.simprints.id.commontesttools.moduleApi

import com.simprints.moduleapi.app.requests.*
import com.simprints.moduleapi.app.responses.*
import kotlinx.parcelize.Parcelize

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
    override val confidenceScore: Int,
    override val tier: IAppResponseTier,
    override val matchConfidence: IAppMatchConfidence
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
internal data class AppEnrolRequestModuleApi(
    override val projectId: String,
    override val userId: String,
    override val moduleId: String,
    override val metadata: String
) : IAppEnrolRequest

@Parcelize
internal data class AppIdentifyRequestModuleApi(
    override val projectId: String,
    override val userId: String,
    override val moduleId: String,
    override val metadata: String
) : IAppIdentifyRequest

@Parcelize
internal data class AppVerifyRequestModuleApi(
    override val projectId: String,
    override val userId: String,
    override val moduleId: String,
    override val metadata: String,
    override val verifyGuid: String
) : IAppVerifyRequest

@Parcelize
internal data class AppConfirmaConfirmIdentityRequestModuleApi(
    override val projectId: String,
    override val userId: String,
    override val sessionId: String,
    override val selectedGuid: String
) : IAppConfirmIdentityRequest

@Parcelize
internal data class AppEnrolLastBiometricsRequestApi(
    override val projectId: String,
    override val userId: String,
    override val moduleId: String,
    override val metadata: String,
    override val sessionId: String
) : IAppEnrolLastBiometricsRequest

fun Pair<String, String>.key() = first
fun Pair<String, String>.value() = second
