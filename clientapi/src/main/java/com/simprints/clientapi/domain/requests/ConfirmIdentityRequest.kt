package com.simprints.clientapi.domain.requests

import com.simprints.moduleapi.app.requests.IAppConfirmIdentityRequest
import kotlinx.parcelize.Parcelize


data class ConfirmIdentityRequest(
    override val projectId: String,
    override val userId: String,
    val sessionId: String,
    val selectedGuid: String,
    override val unknownExtras: Map<String, Any?>
) : BaseRequest {

    override fun convertToAppRequest(): IAppConfirmIdentityRequest = AppConfirmIdentityRequest(
        this.projectId, this.userId, this.sessionId, this.selectedGuid
    )

    @Parcelize
    data class AppConfirmIdentityRequest(
        override val projectId: String,
        override val userId: String,
        override val sessionId: String,
        override val selectedGuid: String
    ) : IAppConfirmIdentityRequest
}


