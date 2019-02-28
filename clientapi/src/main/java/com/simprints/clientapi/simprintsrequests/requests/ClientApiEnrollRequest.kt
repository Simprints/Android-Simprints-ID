package com.simprints.clientapi.simprintsrequests.requests

import com.simprints.moduleinterfaces.app.requests.AppEnrollRequest
import com.simprints.moduleinterfaces.app.requests.AppRequest
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ClientApiEnrollRequest(
    override val projectId: String,
    override val moduleId: String,
    override val userId: String,
    override val metadata: String
) : ClientApiAppRequest {

    override fun convertToAppRequest(): AppRequest = AppEnrollRequest(
        this.projectId, this.userId, this.moduleId, this.metadata
    )

}

@Parcelize
private data class AppEnrollRequest(
    override val projectId: String,
    override val userId: String,
    override val moduleId: String,
    override val metadata: String
) : AppEnrollRequest
