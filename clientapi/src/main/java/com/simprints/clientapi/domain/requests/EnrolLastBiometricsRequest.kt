package com.simprints.clientapi.domain.requests

import com.simprints.core.domain.tokenization.TokenizedString
import com.simprints.moduleapi.app.requests.IAppEnrolLastBiometricsRequest
import com.simprints.moduleapi.app.requests.IAppRequest
import kotlinx.parcelize.Parcelize


data class EnrolLastBiometricsRequest(
    override val projectId: String,
    override val userId: TokenizedString,
    val moduleId: TokenizedString,
    val metadata: String,
    val sessionId: String,
    override val unknownExtras: Map<String, Any?>
) : BaseRequest {

    override fun convertToAppRequest(): IAppRequest = AppEnrolLastBiometricsRequest(
        projectId = this.projectId,
        userId = this.userId.value,
        moduleId = this.moduleId.value,
        metadata = this.metadata,
        sessionId = this.sessionId
    )

    @Parcelize
    private data class AppEnrolLastBiometricsRequest(
        override val projectId: String,
        override val userId: String,
        override val moduleId: String,
        override val metadata: String,
        override val sessionId: String
    ) : IAppEnrolLastBiometricsRequest

}



