package com.simprints.clientapi.domain.responses

import com.simprints.moduleapi.clientapi.responses.IClientApiResponseTier
import com.simprints.moduleapi.clientapi.responses.IClientApiVerifyResponse


data class VerifyResponse(val guid: String,
                          val confidence: Int,
                          val tier: IClientApiResponseTier) {

    constructor(request: IClientApiVerifyResponse) : this(request.guid, request.confidence, request.tier)

}
