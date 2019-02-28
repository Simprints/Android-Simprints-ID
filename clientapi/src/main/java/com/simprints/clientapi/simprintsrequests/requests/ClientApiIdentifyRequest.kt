package com.simprints.clientapi.simprintsrequests.requests

import com.simprints.moduleinterfaces.app.requests.AppIdentifyRequest
import com.simprints.moduleinterfaces.app.requests.AppRequest
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ClientApiIdentifyRequest(
    override val projectId: String,
    override val moduleId: String,
    override val userId: String,
    override val metadata: String
) : ClientApiAppRequest {

    override fun convertToAppRequest(): AppRequest = AppIdentifyRequest(
        this.projectId, this.userId, this.moduleId, this.metadata
    )

}


@Parcelize
private data class AppIdentifyRequest(
    override val projectId: String,
    override val userId: String,
    override val moduleId: String,
    override val metadata: String
) : AppIdentifyRequest
