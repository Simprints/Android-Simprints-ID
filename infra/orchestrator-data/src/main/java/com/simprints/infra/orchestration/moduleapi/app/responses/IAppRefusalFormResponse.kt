package com.simprints.infra.orchestration.moduleapi.app.responses


interface IAppRefusalFormResponse : IAppResponse {

    val reason: String
    val extra: String

}
