package com.simprints.moduleapi.app.requests.confirmations


interface IAppIdentityConfirmationRequest : IAppConfirmation {

    val sessionId: String
    val selectedGuid: String
}
