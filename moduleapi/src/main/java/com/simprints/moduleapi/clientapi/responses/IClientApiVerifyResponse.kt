package com.simprints.moduleapi.clientapi.responses


interface IClientApiVerifyResponse : IClientApiResponse {

    val guid: String
    val confidence: Int
    val tier: IClientApiResponseTier

}
