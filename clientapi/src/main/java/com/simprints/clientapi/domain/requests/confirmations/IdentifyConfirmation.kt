package com.simprints.clientapi.domain.confirmations

import com.simprints.moduleapi.app.requests.confirmations.IAppConfirmation
import com.simprints.moduleapi.app.requests.confirmations.IAppIdentifyConfirmation
import kotlinx.android.parcel.Parcelize


data class IdentifyConfirmation(
    override val projectId: String,
    override val sessionId: String,
    override val selectedGuid: String
) : BaseConfirmation {

    override fun convertToAppRequest(): IAppConfirmation = AppIdentifyConfirmation(
        this.projectId, this.sessionId, this.selectedGuid
    )

    @Parcelize
    data class AppIdentifyConfirmation(
        override val projectId: String,
        override val sessionId: String,
        override val selectedGuid: String
    ) : IAppIdentifyConfirmation

}


