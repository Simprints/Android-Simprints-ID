package com.simprints.clientapi.domain.responses

import com.simprints.moduleapi.app.responses.IAppIdentifyResponse
import com.simprints.moduleapi.app.responses.IAppResponseTier


data class IdentifyResponse(val identifications: List<Identification>,
                            val sessionId: String) {

    data class Identification(val guid: String,
                              val confidence: Int,
                              val tier: IAppResponseTier)

    constructor(request: IAppIdentifyResponse) : this(
        request.identifications.map { Identification(it.guid, it.confidence, it.tier) },
        request.sessionId
    )

}

