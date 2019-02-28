package com.simprints.id.domain.responses

import com.simprints.id.domain.matching.IdentificationResult
import com.simprints.moduleinterfaces.clientapi.responses.IClientApiIdentifyResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IdentificationResponse(val identifications: List<IdentificationResult>,
                                  val sessionId: String): Response {

    //StopShip: it should be an ext, but it's used by MatchAct in Java!
    fun toClientApiResponse() = IClientApiIdentifyResponse (
        identifications.map { ClientApiIdentifyResponse.Identification(it.guidFound, it.confidence, ClientApiTier.valueOf(it.tier.name)) },
        sessionId
    )
}

