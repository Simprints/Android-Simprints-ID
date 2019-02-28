package com.simprints.clientapi.simprintsrequests.requests

import com.simprints.moduleinterfaces.app.requests.AppRequest
import com.simprints.moduleinterfaces.app.requests.AppVerifyRequest
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ClientApiVerifyRequest(
    override val projectId: String,
    override val moduleId: String,
    override val userId: String,
    override val metadata: String,
    val verifyGuid: String
) : ClientApiAppRequest {

    override fun convertToAppRequest(): AppRequest = AppVerifyRequest(
        this.projectId, this.userId, this.moduleId, this.metadata, this.verifyGuid
    )

}

@Parcelize
private data class AppVerifyRequest(
    override val projectId: String,
    override val userId: String,
    override val moduleId: String,
    override val metadata: String,
    override val verifyGuid: String
) : AppVerifyRequest
