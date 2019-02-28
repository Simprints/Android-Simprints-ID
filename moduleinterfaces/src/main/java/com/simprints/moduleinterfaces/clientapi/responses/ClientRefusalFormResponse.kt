package com.simprints.moduleinterfaces.clientapi.responses


interface ClientRefusalFormResponse : ClientResponse {

    val reason: String
    val extra: String

}
