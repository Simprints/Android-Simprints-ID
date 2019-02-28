package com.simprints.id.domain.responses

import com.simprints.clientapi.simprintsrequests.responses.ClientApiIdentifyResponse
import com.simprints.id.domain.matching.IdentificationResult
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AppIdentificationResponse(val identifications: List<IdentificationResult>,
                                     val sessionId: String): AppResponse {

    //StopShip: it should be an ext, but it's used by MatchAct in Java!
    fun toDomainClientApiIdentification() = ClientApiIdentifyResponse(
        identifications.map { ClientApiIdentifyResponse.Identification(it.guidFound, it.confidence, ClientApiTier.valueOf(it.tier.name)) },
        sessionId
    )
}

