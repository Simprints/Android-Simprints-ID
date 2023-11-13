package com.simprints.infra.orchestration.moduleapi.app.responses


interface IAppIdentifyResponse : IAppResponse {

    val identifications: List<IAppMatchResult>
    val sessionId: String
}
