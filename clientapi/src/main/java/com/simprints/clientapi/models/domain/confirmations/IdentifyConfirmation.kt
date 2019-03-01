package com.simprints.clientapi.models.domain.confirmations

import com.simprints.clientapi.models.appinterface.confirmations.AppIdentifyConfirmation
import com.simprints.moduleinterfaces.app.confirmations.IAppConfirmation
import kotlinx.android.parcel.Parcelize


@Parcelize
data class IdentifyConfirmation(
    override val projectId: String,
    override val sessionId: String,
    override val selectedGuid: String
) : BaseConfirmation {

    override fun convertToAppRequest(): IAppConfirmation = AppIdentifyConfirmation(
        this.projectId, this.sessionId, this.selectedGuid
    )

}


