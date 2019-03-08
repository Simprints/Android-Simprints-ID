package com.simprints.id.domain.responses

import com.simprints.id.domain.matching.Tier
import com.simprints.id.domain.matching.toClientApiIClientApiResponseTier
import com.simprints.moduleapi.clientapi.responses.IClientApiResponseTier
import com.simprints.moduleapi.clientapi.responses.IClientApiVerifyResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VerifyResponse(val guid: String,
                          val confidence: Int,
                          val tier: Tier): Response

fun VerifyResponse.toClientApiVerifyResponse(): IClientApiVerifyResponse =
    ClientApiVerifyResponse(guid, confidence, tier.toClientApiIClientApiResponseTier())

@Parcelize
private class ClientApiVerifyResponse(override val guid: String,
                                      override val confidence: Int,
                                      override val tier: IClientApiResponseTier): IClientApiVerifyResponse


