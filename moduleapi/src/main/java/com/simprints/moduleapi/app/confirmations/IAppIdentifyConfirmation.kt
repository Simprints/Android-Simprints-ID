package com.simprints.moduleapi.app.confirmations


interface IAppIdentifyConfirmation : IAppConfirmation {

    val sessionId: String
    val selectedGuid: String

}
