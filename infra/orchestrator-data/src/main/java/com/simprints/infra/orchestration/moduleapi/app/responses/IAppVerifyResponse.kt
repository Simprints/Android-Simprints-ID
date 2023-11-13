package com.simprints.infra.orchestration.moduleapi.app.responses


interface IAppVerifyResponse : IAppResponse {

    val matchResult: IAppMatchResult
}
