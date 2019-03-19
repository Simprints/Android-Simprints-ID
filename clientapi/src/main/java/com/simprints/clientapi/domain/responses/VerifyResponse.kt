package com.simprints.clientapi.domain.responses

import com.simprints.moduleapi.app.responses.IAppResponseTier
import com.simprints.moduleapi.app.responses.IAppVerifyResponse


data class VerifyResponse(val guid: String,
                          val confidence: Int,
                          val tier: IAppResponseTier) {

    constructor(request: IAppVerifyResponse) : this(request.guid, request.confidence, request.tier)

}
