package com.simprints.clientapi.domain.confirmations

import com.simprints.clientapi.domain.ClientBase
import com.simprints.moduleapi.app.confirmations.IAppConfirmation


interface BaseConfirmation : ClientBase {

    val sessionId: String
    val selectedGuid: String

    fun convertToAppRequest(): IAppConfirmation

}
