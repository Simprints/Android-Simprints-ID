package com.simprints.id.domain.requests

import com.simprints.clientapi.simprintsrequests.requests.ClientApiVerifyRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AppVerifyRequest(override val projectId: String,
                           override val userId: String,
                           override val moduleId: String,
                           override val metadata: String,
                           val verifyGuid: String) : AppRequest {

    constructor(clientApiVerifyRequest: ClientApiVerifyRequest) : this(
        projectId = clientApiVerifyRequest.projectId,
        userId = clientApiVerifyRequest.userId,
        moduleId = clientApiVerifyRequest.moduleId,
        metadata = clientApiVerifyRequest.metadata,
        verifyGuid = clientApiVerifyRequest.verifyGuid
    )
}

fun ClientApiVerifyRequest.toDomainIdVerifyRequest() = AppVerifyRequest(this)
fun AppRequest.isVerifyRequest() = this is AppVerifyRequest
