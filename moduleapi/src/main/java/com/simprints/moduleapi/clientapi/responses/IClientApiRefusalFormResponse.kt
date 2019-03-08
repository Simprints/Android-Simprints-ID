package com.simprints.moduleapi.clientapi.responses


interface IClientApiRefusalFormResponse : IClientApiResponse {

    val reason: String
    val extra: String

}
