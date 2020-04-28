package com.simprints.moduleapi.app.requests


interface IAppIdentityConfirmationRequest : IAppRequest {

    val sessionId: String
    val selectedGuid: String
}
