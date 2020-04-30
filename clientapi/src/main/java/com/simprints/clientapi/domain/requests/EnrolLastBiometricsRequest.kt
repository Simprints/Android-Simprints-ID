package com.simprints.clientapi.domain.requests

import com.simprints.moduleapi.app.requests.IAppEnrolLastBiometricsRequest
import com.simprints.moduleapi.app.requests.IAppRequest
import kotlinx.android.parcel.Parcelize


data class EnrolLastBiometricsRequest(
    override val projectId: String,
    val moduleId: String,
    override val userId: String,
    val metadata: String,
    val sessionId: String,
    override val unknownExtras: Map<String, Any?>
) : BaseRequest {

    override fun convertToAppRequest(): IAppRequest = AppEnrolLastBiometricsRequest(
        this.projectId, this.userId, this.moduleId, this.metadata, this.sessionId
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



