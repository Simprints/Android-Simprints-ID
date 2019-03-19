package com.simprints.moduleapi.app.responses


interface IAppRefusalFormResponse : IAppResponse {

    val reason: String
    val extra: String

}
