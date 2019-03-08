package com.simprints.clientapi.domain.requests

import com.simprints.moduleapi.app.requests.IAppRequest
import com.simprints.moduleapi.app.requests.IAppVerifyRequest
import kotlinx.android.parcel.Parcelize


data class VerifyRequest(
    override val projectId: String,
    override val moduleId: String,
    override val userId: String,
    override val metadata: String,
    val verifyGuid: String
) : BaseRequest {

    override fun convertToAppRequest(): IAppRequest = AppVerifyRequest(
        this.projectId, this.userId, this.moduleId, this.metadata, this.verifyGuid
    )

    @Parcelize
    private data class AppVerifyRequest(
        override val projectId: String,
        override val userId: String,
        override val moduleId: String,
        override val metadata: String,
        override val verifyGuid: String
    ) : IAppVerifyRequest

}

