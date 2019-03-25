package com.simprints.moduleapi.app.responses


interface IAppIdentifyResponse : IAppResponse {

    val identifications: List<IAppMatchResult>
    val sessionId: String
}
