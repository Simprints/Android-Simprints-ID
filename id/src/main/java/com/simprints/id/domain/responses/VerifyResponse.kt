package com.simprints.id.domain.responses

import com.simprints.id.domain.matching.Tier
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VerifyResponse(val guid: String, val confidence: Int, val tier: Tier): Response {

    //StopShip: it should be an ext, but it's used by MatchAct in Java!
    fun toDomainClientApiVerify() = ClientApiVerifyResponse(guid, confidence, ClientApiTier.valueOf(tier.name))
}
