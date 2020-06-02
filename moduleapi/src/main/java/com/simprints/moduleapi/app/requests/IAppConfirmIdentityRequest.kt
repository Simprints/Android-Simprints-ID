package com.simprints.moduleapi.app.requests


interface IAppConfirmIdentityRequest : IAppRequest {

    val sessionId: String
    val selectedGuid: String
}
