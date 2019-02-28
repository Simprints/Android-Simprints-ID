package com.simprints.moduleinterfaces.app.confirmations


interface AppIdentifyConfirmation : AppConfirmation {

    val sessionId: String
    val selectedGuid: String

}
