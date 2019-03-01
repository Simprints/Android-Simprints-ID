package com.simprints.clientapi.models.domain.requests

import com.simprints.clientapi.models.appinterface.requests.AppIdentifyRequest
import com.simprints.moduleinterfaces.app.requests.IAppRequest
import kotlinx.android.parcel.Parcelize


@Parcelize
data class IdentifyRequest(
    override val projectId: String,
    override val moduleId: String,
    override val userId: String,
    override val metadata: String
) : BaseRequest {

    override fun convertToAppRequest(): IAppRequest = AppIdentifyRequest(
        this.projectId, this.userId, this.moduleId, this.metadata
    )

}


