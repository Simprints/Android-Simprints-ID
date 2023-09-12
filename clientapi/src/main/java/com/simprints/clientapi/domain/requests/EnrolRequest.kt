package com.simprints.clientapi.domain.requests

import com.simprints.core.domain.tokenization.TokenizableString
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
        moduleId = this.moduleId.value,
        metadata = this.metadata
    )

    @Parcelize
    private data class AppEnrolRequest(
        override val projectId: String,
        override val userId: String,
        override val moduleId: String,
        override val metadata: String
    ) : IAppEnrolRequest

}


