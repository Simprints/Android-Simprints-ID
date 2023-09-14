package com.simprints.clientapi.domain.requests

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.isTokenized
import com.simprints.moduleapi.app.requests.IAppRequest
import com.simprints.moduleapi.app.requests.IAppVerifyRequest
import kotlinx.parcelize.Parcelize


data class VerifyRequest(
    override val projectId: String,
    override val userId: TokenizableString,
    val moduleId: TokenizableString,
    val metadata: String,
    val verifyGuid: String,
    override val unknownExtras: Map<String, Any?>
) : BaseRequest {

    override fun convertToAppRequest(): IAppRequest = AppVerifyRequest(
        projectId = this.projectId,
        userId = this.userId.value,
        isUserIdTokenized = this.userId.isTokenized(),
        moduleId = this.moduleId.value,
        isModuleIdTokenized = this.moduleId.isTokenized(),
        metadata = this.metadata,
        verifyGuid = this.verifyGuid,
    )

    @Parcelize
    private data class AppVerifyRequest(
        override val projectId: String,
        override val userId: String,
        override val isUserIdTokenized: Boolean,
        override val moduleId: String,
        override val isModuleIdTokenized: Boolean,
        override val metadata: String,
        override val verifyGuid: String
    ) : IAppVerifyRequest

}

