package com.simprints.clientapi.domain.requests

import com.simprints.core.domain.tokenization.TokenizedString
import com.simprints.moduleapi.app.requests.IAppConfirmIdentityRequest
import kotlinx.parcelize.Parcelize


data class ConfirmIdentityRequest(
    override val projectId: String,
    override val userId: TokenizedString,
    val sessionId: String,
    val selectedGuid: String,
    override val unknownExtras: Map<String, Any?>
) : BaseRequest {

    override fun convertToAppRequest(): IAppConfirmIdentityRequest = AppConfirmIdentityRequest(
        projectId = this.projectId,
        userId = this.userId.value,
        sessionId = this.sessionId,
        selectedGuid = this.selectedGuid
    )

    @Parcelize
    data class AppConfirmIdentityRequest(
        override val projectId: String,
        override val userId: String,
        override val sessionId: String,
        override val selectedGuid: String
    ) : IAppConfirmIdentityRequest
}


