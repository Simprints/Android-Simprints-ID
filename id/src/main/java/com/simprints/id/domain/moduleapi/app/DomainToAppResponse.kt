package com.simprints.id.domain.moduleapi.app

import android.os.Parcelable
import com.simprints.id.domain.moduleapi.app.responses.*
import com.simprints.id.domain.moduleapi.app.responses.AppErrorType.*
import com.simprints.id.domain.moduleapi.app.responses.entities.MatchResult
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier
import com.simprints.moduleapi.app.responses.*
import kotlinx.android.parcel.Parcelize
import com.simprints.id.domain.moduleapi.app.responses.AppResponseType.*
import kotlinx.android.parcel.IgnoredOnParcel

object DomainToAppResponse {

    fun fromDomainToAppResponse(response: AppResponse): IAppResponse =
        when (response.type) {
            ENROL -> fromDomainToAppEnrolResponse(response as AppEnrolResponse)
            IDENTIFY -> fromDomainToAppIdentifyResponse(response as AppIdentifyResponse)
            REFUSAL -> fromDomainToAppRefusalFormResponse(response as AppRefusalFormResponse)
            VERIFY -> fromDomainToAppVerifyResponse(response as AppVerifyResponse)
            ERROR -> fromDomainToAppErrorResponse(response as AppErrorResponse)
        }

    private fun fromDomainToAppErrorResponse(response: AppErrorResponse): IAppResponse =
        when (response.error) {
            DIFFERENT_PROJECT_ID_SIGNED_IN -> IAppErrorType.DIFFERENT_PROJECT_ID_SIGNED_IN
            DIFFERENT_USER_ID_SIGNED_IN -> IAppErrorType.DIFFERENT_USER_ID_SIGNED_IN
            GUID_NOT_FOUND_ONLINE -> IAppErrorType.GUID_NOT_FOUND_ONLINE
            GUID_NOT_FOUND_OFFLINE ->IAppErrorType.GUID_NOT_FOUND_OFFLINE
            UNEXPECTED_ERROR -> IAppErrorType.UNEXPECTED_ERROR
            BLUETOOTH_NOT_SUPPORTED -> IAppErrorType.BLUETOOTH_NOT_SUPPORTED
            SCANNER_LOW_BATTERY -> IAppErrorType.SCANNER_LOW_BATTERY
            UNKNOWN_BLUETOOTH_ISSUE -> IAppErrorType.UNKNOWN_BLUETOOTH_ISSUE
        }.let {
            IAppErrorResponseImpl(it)
        }

    private fun fromDomainToAppEnrolResponse(enrol: AppEnrolResponse): IAppEnrolResponse = IAppEnrolResponseImpl(enrol.guid)

    private fun fromDomainToAppVerifyResponse(verify: AppVerifyResponse): IAppVerifyResponse =
        with(verify.matchingResult) {
            IAppVerifyResponseImpl(IAppMatchResultImpl(guidFound, confidence, fromDomainToAppIAppResponseTier(tier)))
        }

    private fun fromDomainToAppIdentifyResponse(identify: AppIdentifyResponse): IAppIdentifyResponse =
        IAppIdentifyResponseImpl(identify.identifications.map { fromDomainToAppMatchResult(it) }, identify.sessionId)

    private fun fromDomainToAppRefusalFormResponse(refusaResponse: AppRefusalFormResponse): IAppRefusalFormResponse =
        IAppRefusalFormResponseImpl(refusaResponse.answer.reason.toString(), refusaResponse.answer.optionalText)

    private fun fromDomainToAppMatchResult(result: MatchResult): IAppMatchResult =
        IAppMatchResultImpl(result.guidFound, result.confidence, fromDomainToAppIAppResponseTier(result.tier))

    private fun fromDomainToAppIAppResponseTier(tier: Tier): IAppResponseTier =
        when (tier) {
            Tier.TIER_1 -> IAppResponseTier.TIER_1
            Tier.TIER_2 -> IAppResponseTier.TIER_2
            Tier.TIER_3 -> IAppResponseTier.TIER_3
            Tier.TIER_4 -> IAppResponseTier.TIER_4
            Tier.TIER_5 -> IAppResponseTier.TIER_5
        }
}

@Parcelize
private class IAppEnrolResponseImpl(override val guid: String) : IAppEnrolResponse {
    @IgnoredOnParcel override val type: IAppResponseType = IAppResponseType.ENROL
}

@Parcelize
private class IAppErrorResponseImpl(override val error: IAppErrorType) : IAppErrorResponse {
    @IgnoredOnParcel override val type: IAppResponseType = IAppResponseType.ERROR
}

@Parcelize
private class IAppIdentifyResponseImpl(override val identifications: List<IAppMatchResult>,
                                       override val sessionId: String) : IAppIdentifyResponse {

    @IgnoredOnParcel override val type: IAppResponseType = IAppResponseType.IDENTIFY
}

@Parcelize
private class IAppRefusalFormResponseImpl(
    override val reason: String,
    override val extra: String) : IAppRefusalFormResponse {
    @IgnoredOnParcel override val type: IAppResponseType = IAppResponseType.REFUSAL
}

@Parcelize
private class IAppVerifyResponseImpl(override val matchResult: IAppMatchResult) : IAppVerifyResponse {
    @IgnoredOnParcel override val type: IAppResponseType = IAppResponseType.VERIFY
}

@Parcelize
private data class IAppMatchResultImpl(
    override val guid: String,
    override val confidence: Int,
    override val tier: IAppResponseTier) : Parcelable, IAppMatchResult
