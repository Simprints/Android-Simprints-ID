package com.simprints.moduleinterfaces.clientapi.responses


interface IClientApiRefusalFormResponse : IClientApiResponse {

    val reason: String
    val extra: String

}
