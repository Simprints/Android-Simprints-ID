package com.simprints.clientapi.domain.requests.confirmations

import com.simprints.moduleapi.app.requests.confirmations.IAppConfirmation
import com.simprints.moduleapi.app.requests.confirmations.IAppIdentityConfirmationRequest
import kotlinx.android.parcel.Parcelize


data class IdentityConfirmation(
    override val projectId: String,
    override val sessionId: String,
    override val selectedGuid: String,
    override val unknownExtras: Map<String, Any?>
) : BaseConfirmation {

    override fun convertToAppRequest(): IAppConfirmation = AppIdentityConfirmationRequest(
        this.projectId, this.sessionId, this.selectedGuid
    )

    @Parcelize
    data class AppIdentityConfirmationRequest(
        override val projectId: String,
        override val sessionId: String,
        override val selectedGuid: String
    ) : IAppIdentityConfirmationRequest
}


