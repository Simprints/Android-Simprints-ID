package com.simprints.clientapi.domain.requests

import com.simprints.moduleapi.app.requests.IAppIdentityConfirmationRequest
import kotlinx.android.parcel.Parcelize


data class IdentityConfirmationRequest(
    override val projectId: String,
    override val userId: String,
    val sessionId: String,
    val selectedGuid: String,
    override val unknownExtras: Map<String, Any?>
) : BaseRequest {

    override fun convertToAppRequest(): IAppIdentityConfirmationRequest = AppIdentityConfirmationRequest(
        this.projectId, this.userId, this.sessionId, this.selectedGuid
    )

    @Parcelize
    data class AppIdentityConfirmationRequest(
        override val projectId: String,
        override val userId: String,
        override val sessionId: String,
        override val selectedGuid: String
    ) : IAppIdentityConfirmationRequest
}


