package com.simprints.clientapi.domain.requests

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.isTokenized
import com.simprints.moduleapi.app.requests.IAppIdentifyRequest
import com.simprints.moduleapi.app.requests.IAppRequest
import kotlinx.parcelize.Parcelize


data class IdentifyRequest(
    override val projectId: String,
    override val userId: TokenizableString,
    val moduleId: TokenizableString,
    val metadata: String,
    override val unknownExtras: Map<String, Any?>
) : BaseRequest {

    override fun convertToAppRequest(): IAppRequest = AppIdentifyRequest(
        projectId = this.projectId,
        userId = this.userId.value,
        moduleId = this.moduleId.value,
        metadata = this.metadata,
        isUserIdTokenized = this.userId.isTokenized(),
        isModuleIdTokenized = this.moduleId.isTokenized()
    )

    @Parcelize
    data class AppIdentifyRequest(
        override val projectId: String,
        override val userId: String,
        override val isUserIdTokenized: Boolean,
        override val moduleId: String,
        override val isModuleIdTokenized: Boolean,
        override val metadata: String
    ) : IAppIdentifyRequest
}


