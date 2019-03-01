package com.simprints.clientapi.models.domain.confirmations

import android.os.Parcelable
import com.simprints.clientapi.models.domain.ClientBase
import com.simprints.moduleinterfaces.app.confirmations.IAppConfirmation


interface BaseConfirmation : ClientBase, Parcelable {

    val sessionId: String
    val selectedGuid: String

    fun convertToAppRequest(): IAppConfirmation

}
