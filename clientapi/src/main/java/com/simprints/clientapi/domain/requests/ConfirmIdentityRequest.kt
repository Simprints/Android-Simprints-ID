package com.simprints.clientapi.domain.requests

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.isTokenized
import com.simprints.moduleapi.app.requests.IAppConfirmIdentityRequest
import kotlinx.parcelize.Parcelize


data class ConfirmIdentityRequest(
    override val projectId: String,
    override val userId: TokenizableString,
    val sessionId: String,
    val selectedGuid: String,
    override val unknownExtras: Map<String, Any?>
) : BaseRequest {

    override fun convertToAppRequest(): IAppConfirmIdentityRequest = AppConfirmIdentityRequest(
        projectId = this.projectId,
        userId = this.userId.value,
        isUserIdTokenized = this.userId.isTokenized(),
        sessionId = this.sessionId,
        selectedGuid = this.selectedGuid
    )

    @Parcelize
    data class AppConfirmIdentityRequest(
        override val projectId: String,
        override val userId: String,
        override val isUserIdTokenized: Boolean,
        override val sessionId: String,
        override val selectedGuid: String
    ) : IAppConfirmIdentityRequest
}

