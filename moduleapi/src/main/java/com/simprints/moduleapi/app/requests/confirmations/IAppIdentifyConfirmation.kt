package com.simprints.moduleapi.app.requests.confirmations


interface IAppIdentifyConfirmation : IAppConfirmation {

    val sessionId: String
    val selectedGuid: String

}
