package com.simprints.id.domain.responses

import com.simprints.clientapi.simprintsrequests.responses.ClientApiTier
import com.simprints.clientapi.simprintsrequests.responses.ClientApiVerifyResponse

data class IdVerifyResponse(val guid: String, val confidence: Int, val tier: TierResponse): IdResponse {

    //StopShip: it should be an ext, but it's used by MatchAct in Java!
    fun toDomainClientApiVerify() = ClientApiVerifyResponse(guid, confidence, ClientApiTier.valueOf(tier.name))
}
