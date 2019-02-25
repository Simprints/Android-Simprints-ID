package com.simprints.id.domain.responses

import com.simprints.clientapi.simprintsrequests.responses.Tier
import com.simprints.clientapi.simprintsrequests.responses.VerifyResponse

data class IdVerifyResponse(val guid: String, val confidence: Int, val tier: TierResponse): IdResponse {

    //StopShip: it should be an ext, but it's used by MatchAct in Java!
    fun toDomainClientApiVerify() = VerifyResponse(guid, confidence, Tier.valueOf(tier.name))
}
