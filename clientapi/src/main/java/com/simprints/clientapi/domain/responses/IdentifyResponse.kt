package com.simprints.clientapi.domain.responses

import com.simprints.moduleinterfaces.clientapi.responses.IClientApiIdentifyResponse
import com.simprints.moduleinterfaces.clientapi.responses.IClientApiResponseTier


data class IdentifyResponse(val identifications: List<Identification>,
                            val sessionId: String) {

    data class Identification(val guid: String,
                              val confidence: Int,
                              val tier: IClientApiResponseTier)

    constructor(request: IClientApiIdentifyResponse) : this(
        request.identifications.map { Identification(it.guid, it.confidence, it.tier) },
        request.sessionId
    )

}

