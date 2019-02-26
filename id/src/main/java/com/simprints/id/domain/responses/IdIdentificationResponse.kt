package com.simprints.id.domain.responses

import com.simprints.clientapi.simprintsrequests.responses.ClientApiIdentifyResponse
import com.simprints.clientapi.simprintsrequests.responses.ClientApiTier
import com.simprints.id.domain.identification.IdentificationResult

data class IdIdentificationResponse(val identifications: List<IdentificationResult>,
                                    val sessionId: String): IdResponse {

    //StopShip: it should be an ext, but it's used by MatchAct in Java!
    fun toDomainClientApiIdentification() = ClientApiIdentifyResponse(
        identifications.map { ClientApiIdentifyResponse.Identification(it.guidFound, it.confidence, ClientApiTier.valueOf(it.tier.name)) },
        sessionId
    )
}

