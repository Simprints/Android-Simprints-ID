package com.simprints.id.domain.responses

import com.simprints.clientapi.simprintsrequests.responses.IdentificationResponse
import com.simprints.clientapi.simprintsrequests.responses.Tier
import com.simprints.libsimprints.Identification

data class IdIdentificationResponse(val identifications: List<IdIdentification>,
                                    val sessionId: String): IdResponse {

    constructor(topCandidates: ArrayList<Identification>, sessionId: String):
        this(topCandidates.map { IdIdentification(it.guid, it.confidence.toInt(), TierResponse.valueOf(it.tier.name)) }, sessionId)

    data class IdIdentification(val guid: String, val confidence: Int, val tier: TierResponse) //StopShip: we may not need it, we can use LibSimprints

    //StopShip: it should be an ext, but it's used by MatchAct in Java!
    fun toDomainClientApiIdentification() = IdentificationResponse(
        identifications.map { IdentificationResponse.Identification(it.guid, it.confidence, Tier.valueOf(it.tier.name)) },
        sessionId
    )
}

