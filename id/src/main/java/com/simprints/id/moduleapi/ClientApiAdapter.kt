package com.simprints.id.moduleapi

import android.os.Parcelable
import com.simprints.id.domain.matching.IdentificationResult
import com.simprints.id.domain.matching.Tier
import com.simprints.id.domain.responses.*
import com.simprints.moduleapi.app.responses.*
import kotlinx.android.parcel.Parcelize

object fromDomainToClientApiAdapter {

    fun fromDomainToClientApiResponse(response: Response?): IAppResponse? =
        when(response) {
            is EnrolResponse -> fromDomainToClientApiEnrolResponse(response)
            is IdentifyResponse -> fromDomainToClientApiIdentifyResponse(response)
            is VerifyResponse -> fromDomainToClientApiVerifyResponse(response)
            else -> null
        }

    private fun fromDomainToClientApiEnrolResponse(enrol: EnrolResponse): IAppEnrolResponse = ClientApiEnrollResponse(enrol.guid)

    private fun fromDomainToClientApiVerifyResponse(verify: VerifyResponse): IAppVerifyResponse =
        ClientApiVerifyResponse(verify.guid, verify.confidence, fromDomainToClientApiIClientApiResponseTier(verify.tier))

    private fun fromDomainToClientApiIdentifyResponse(identify: IdentifyResponse): IAppIdentifyResponse =
        ClientApiIdentifyResponse(identify.identifications.map { fromDomainToClientApiIdentificationResult(it) }, identify.sessionId)

    fun fromDomainToClientApiRefusalFormResponse(refusaResponse: RefusalFormResponse): IAppRefusalFormResponse =
        ClientApiRefusalFormResponse(refusaResponse.answer.reason.toString(), refusaResponse.answer.optionalText)

    private fun fromDomainToClientApiIClientApiResponseTier(tier: Tier): IAppResponseTier =
        when (tier) {
            Tier.TIER_1 -> IAppResponseTier.TIER_1
            Tier.TIER_2 -> IAppResponseTier.TIER_2
            Tier.TIER_3 -> IAppResponseTier.TIER_3
            Tier.TIER_4 -> IAppResponseTier.TIER_4
            Tier.TIER_5 -> IAppResponseTier.TIER_5
        }

    private fun fromDomainToClientApiIdentificationResult(result: IdentificationResult): IAppIdentifyResponse.IIdentificationResult =
        ClientApiIdentificationResult(result.guidFound, result.confidence, fromDomainToClientApiIClientApiResponseTier(result.tier))
}

@Parcelize
private class ClientApiEnrollResponse(override val guid: String) : IAppEnrolResponse

@Parcelize
private class ClientApiIdentifyResponse(override val identifications: List<IAppIdentifyResponse.IIdentificationResult>,
                                        override val sessionId: String) : IAppIdentifyResponse

@Parcelize
private class ClientApiRefusalFormResponse(
    override val reason: String,
    override val extra: String) : IAppRefusalFormResponse

@Parcelize
private class ClientApiVerifyResponse(override val guid: String,
                                      override val confidence: Int,
                                      override val tier: IAppResponseTier) : IAppVerifyResponse

@Parcelize
private data class ClientApiIdentificationResult(
    override val guid: String,
    override val confidence: Int,
    override val tier: IAppResponseTier) : Parcelable, IAppIdentifyResponse.IIdentificationResult
