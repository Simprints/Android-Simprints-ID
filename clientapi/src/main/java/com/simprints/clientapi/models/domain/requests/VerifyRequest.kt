package com.simprints.clientapi.models.domain.requests

import com.simprints.clientapi.models.appinterface.requests.AppVerifyRequest
import com.simprints.moduleinterfaces.app.requests.IAppRequest
import kotlinx.android.parcel.Parcelize


@Parcelize
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

}

