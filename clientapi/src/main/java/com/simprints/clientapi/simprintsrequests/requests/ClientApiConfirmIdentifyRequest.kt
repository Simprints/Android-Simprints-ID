package com.simprints.clientapi.simprintsrequests.requests

import com.simprints.moduleinterfaces.app.confirmations.IAppConfirmation
import com.simprints.moduleinterfaces.app.confirmations.IAppIdentifyConfirmation
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ClientApiConfirmIdentifyRequest(
    override val projectId: String,
    override val sessionId: String,
    override val selectedGuid: String
) : ClientApiAppConfirmation {

    override fun convertToAppRequest(): IAppConfirmation = AppIdentifyConfirmation(
        this.projectId, this.sessionId, this.selectedGuid
    )

}

@Parcelize
private data class AppIdentifyConfirmation(
    override val projectId: String,
    override val sessionId: String,
    override val selectedGuid: String
) : IAppIdentifyConfirmation
