package com.simprints.moduleinterfaces.app.confirmations


interface IAppIdentifyConfirmation : IAppConfirmation {

    val sessionId: String
    val selectedGuid: String

}
