package com.simprints.clientapi.domain.requests

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.isTokenized
import com.simprints.moduleapi.app.requests.IAppEnrolRequest
import com.simprints.moduleapi.app.requests.IAppRequest
import kotlinx.parcelize.Parcelize


data class EnrolRequest(
    override val projectId: String,
    override val userId: TokenizableString,
    val moduleId: TokenizableString,
    val metadata: String,
    override val unknownExtras: Map<String, Any?>
) : BaseRequest {

    override fun convertToAppRequest(): IAppRequest = AppEnrolRequest(
        projectId = this.projectId,
        userId = this.userId.value,
        isUserIdTokenized = this.userId.isTokenized(),
        moduleId = this.moduleId.value,
        isModuleIdTokenized = this.moduleId.isTokenized(),
        metadata = this.metadata
    )

    @Parcelize
    private data class AppEnrolRequest(
        override val projectId: String,
        override val userId: String,
        override val isUserIdTokenized: Boolean,
        override val moduleId: String,
        override val isModuleIdTokenized: Boolean,
        override val metadata: String
    ) : IAppEnrolRequest

}


