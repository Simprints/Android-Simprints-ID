package com.simprints.moduleapi.app.responses


interface IAppVerifyResponse : IAppResponse {

    val guid: String
    val confidence: Int
    val tier: IAppResponseTier

}
