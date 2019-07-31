package com.simprints.moduleapi.app.requests.confirmations


interface IAppIdentifyConfirmationRequest : IAppConfirmation {

    val sessionId: String
    val selectedGuid: String
}
