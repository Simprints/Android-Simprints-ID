package com.simprints.moduleinterfaces.clientapi.responses


interface IClientApiVerifyResponse : IClientApiResponse {

    val guid: String
    val confidence: Int
    val tier: IClientApiResponseTier

}
