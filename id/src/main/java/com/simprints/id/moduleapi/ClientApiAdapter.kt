package com.simprints.id.moduleapi

import android.os.Parcelable
import com.simprints.id.domain.matching.IdentificationResult
import com.simprints.id.domain.matching.Tier
import com.simprints.id.domain.responses.EnrolResponse
import com.simprints.id.domain.responses.IdentifyResponse
import com.simprints.id.domain.responses.RefusalFormResponse
import com.simprints.id.domain.responses.VerifyResponse
import com.simprints.moduleapi.clientapi.responses.*
import kotlinx.android.parcel.Parcelize

object ClientApiAdapter {

    fun toClientApiEnrolResponse(enrol: EnrolResponse): IClientApiEnrolResponse = ClientApiEnrollResponse(enrol.guid)

    fun toClientApiVerifyResponse(verify: VerifyResponse): IClientApiVerifyResponse =
        ClientApiVerifyResponse(verify.guid, verify.confidence, toClientApiIClientApiResponseTier(verify.tier))

    fun toClientApiIdentifyResponse(identify: IdentifyResponse): IClientApiIdentifyResponse =
        ClientApiIdentifyResponse(identify.identifications.map { toClientApiIdentificationResult(it) }, identify.sessionId)

    fun toClientApiRefusalFormResponse(refusaResponse: RefusalFormResponse): IClientApiRefusalFormResponse =
        ClientApiRefusalFormResponse(refusaResponse.answer.reason.toString(), refusaResponse.answer.optionalText)

    private fun toClientApiIClientApiResponseTier(tier: Tier): IClientApiResponseTier =
        when (tier) {
            Tier.TIER_1 -> IClientApiResponseTier.TIER_1
            Tier.TIER_2 -> IClientApiResponseTier.TIER_2
            Tier.TIER_3 -> IClientApiResponseTier.TIER_3
            Tier.TIER_4 -> IClientApiResponseTier.TIER_4
            Tier.TIER_5 -> IClientApiResponseTier.TIER_5
        }

    private fun toClientApiIdentificationResult(result: IdentificationResult): IClientApiIdentifyResponse.IIdentificationResult =
        ClientApiIdentificationResult(result.guidFound, result.confidence, toClientApiIClientApiResponseTier(result.tier))
}

@Parcelize
private class ClientApiEnrollResponse(override val guid: String) : IClientApiEnrolResponse

@Parcelize
private class ClientApiIdentifyResponse(override val identifications: List<IClientApiIdentifyResponse.IIdentificationResult>,
                                        override val sessionId: String) : IClientApiIdentifyResponse

@Parcelize
private class ClientApiRefusalFormResponse(
    override val reason: String,
    override val extra: String) : IClientApiRefusalFormResponse

@Parcelize
private class ClientApiVerifyResponse(override val guid: String,
                                      override val confidence: Int,
                                      override val tier: IClientApiResponseTier) : IClientApiVerifyResponse

@Parcelize
private data class ClientApiIdentificationResult(
    override val guid: String,
    override val confidence: Int,
    override val tier: IClientApiResponseTier) : Parcelable, IClientApiIdentifyResponse.IIdentificationResult
