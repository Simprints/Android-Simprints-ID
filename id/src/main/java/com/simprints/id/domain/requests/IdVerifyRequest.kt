package com.simprints.id.domain.requests

import com.simprints.clientapi.simprintsrequests.requests.ClientApiVerifyRequest

data class IdVerifyRequest(override val projectId: String,
                           override val userId: String,
                           override val moduleId: String,
                           override val metadata: String,
                           val verifyGuid: String) : IdRequest {

    constructor(clientApiVerifyRequest: ClientApiVerifyRequest) : this(
        projectId = clientApiVerifyRequest.projectId,
        userId = clientApiVerifyRequest.userId,
        moduleId = clientApiVerifyRequest.moduleId,
        metadata = clientApiVerifyRequest.metadata,
        verifyGuid = clientApiVerifyRequest.verifyGuid
    )
}

fun ClientApiVerifyRequest.toDomainIdVerifyRequest() = IdVerifyRequest(this)
fun IdRequest.isVerifyRequest() = this is IdVerifyRequest
