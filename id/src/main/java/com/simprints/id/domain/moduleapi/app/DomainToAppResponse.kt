package com.simprints.id.domain.moduleapi.app

import android.os.Parcelable
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.app.responses.AppIdentifyResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.AppVerifyResponse
import com.simprints.id.domain.moduleapi.app.responses.entities.MatchResult
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier
import com.simprints.moduleapi.app.responses.*
import kotlinx.android.parcel.Parcelize

object DomainToAppResponse {

    fun fromDomainToAppResponse(response: AppResponse?): IAppResponse? =
        when(response) {
            is AppEnrolResponse -> fromDomainToAppEnrolResponse(response)
            is AppIdentifyResponse -> fromDomainToAppIdentifyResponse(response)
            is AppVerifyResponse -> fromDomainToAppVerifyResponse(response)
            is com.simprints.id.domain.moduleapi.app.responses.AppRefusalFormResponse -> fromDomainToAppRefusalFormResponse(response)
            else -> null
        }

    private fun fromDomainToAppEnrolResponse(enrol: AppEnrolResponse): IAppEnrolResponse = IAppEnrolResponseImpl(enrol.guid)

    private fun fromDomainToAppVerifyResponse(verify: AppVerifyResponse): IAppVerifyResponse =
        with(verify.matchingResult) {
            IAppVerifyResponseImpl(IAppMatchResultImpl(guidFound, confidence, fromDomainToAppIAppResponseTier(tier)))
        }
    private fun fromDomainToAppIdentifyResponse(identify: AppIdentifyResponse): IAppIdentifyResponse =
        IAppIdentifyResponseImpl(identify.identifications.map { fromDomainToAppMatchResult(it) }, identify.sessionId)

    private fun fromDomainToAppRefusalFormResponse(refusaResponse: com.simprints.id.domain.moduleapi.app.responses.AppRefusalFormResponse): IAppRefusalFormResponse =
        AppRefusalFormResponse(refusaResponse.answer.reason.toString(), refusaResponse.answer.optionalText)

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
private class IAppEnrolResponseImpl(override val guid: String) : IAppEnrolResponse

@Parcelize
private class IAppIdentifyResponseImpl(override val identifications: List<IAppMatchResult>,
                                        override val sessionId: String) : IAppIdentifyResponse

@Parcelize
private class AppRefusalFormResponse(
    override val reason: String,
    override val extra: String) : IAppRefusalFormResponse

@Parcelize
private class IAppVerifyResponseImpl(override val matchResult: IAppMatchResult) : IAppVerifyResponse

@Parcelize
private data class IAppMatchResultImpl(
    override val guid: String,
    override val confidence: Int,
    override val tier: IAppResponseTier) : Parcelable, IAppMatchResult
