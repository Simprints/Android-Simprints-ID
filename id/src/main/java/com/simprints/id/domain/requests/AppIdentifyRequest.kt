package com.simprints.id.domain.requests

import com.simprints.clientapi.simprintsrequests.requests.ClientApiIdentifyRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AppIdentifyRequest(override val projectId: String,
                              override val userId: String,
                              override val moduleId: String,
                              override val metadata: String) : AppRequest {

    constructor(clientApiVerifyRequest: ClientApiIdentifyRequest) : this(
        projectId = clientApiVerifyRequest.projectId,
        userId = clientApiVerifyRequest.userId,
        moduleId = clientApiVerifyRequest.moduleId,
        metadata = clientApiVerifyRequest.metadata
    )
}

fun ClientApiIdentifyRequest.toDomainIdIdentifyRequest() = AppIdentifyRequest(this)
fun AppRequest.isIdentifyRequest() = this is AppIdentifyRequest
