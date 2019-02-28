package com.simprints.moduleinterfaces.clientapi.responses


interface ClientVerifyResponse : ClientResponse {

    val guid: String
    val confidence: Int
    val tier: ClientResponseTier

}
