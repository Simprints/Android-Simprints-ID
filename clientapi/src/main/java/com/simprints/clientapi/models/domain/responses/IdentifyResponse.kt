package com.simprints.clientapi.models.domain.responses

import com.simprints.clientapi.models.appinterface.responses.AppIdentifyResponse
import com.simprints.moduleinterfaces.clientapi.responses.IClientApiResponseTier


data class IdentifyResponse(val identifications: List<Identification>,
                            val sessionId: String) {

    data class Identification(val guid: String,
                              val confidence: Int,
                              val tier: IClientApiResponseTier)

    constructor(request: AppIdentifyResponse) : this(
        request.identifications.map { Identification(it.guid, it.confidence, it.tier) },
        request.sessionId
    )

}

