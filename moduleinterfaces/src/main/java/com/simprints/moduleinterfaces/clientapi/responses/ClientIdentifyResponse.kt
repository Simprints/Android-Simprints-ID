package com.simprints.moduleinterfaces.clientapi.responses


interface ClientIdentifyResponse : ClientResponse {

    val identifications: List<Identification>
    val sessionId: String

    interface Identification {
        val guid: String
        val confidence: Int
        val tier: ClientResponseTier
    }

}
