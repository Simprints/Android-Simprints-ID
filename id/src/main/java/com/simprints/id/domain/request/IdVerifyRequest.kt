package com.simprints.id.domain.request

import com.simprints.clientapi.simprintsrequests.requests.ClientApiVerifyRequest

class IdVerifyRequest(override val projectId: String,
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

fun ClientApiVerifyRequest.toIdDomainIdVerifyRequest() = IdVerifyRequest(this)
fun IdRequest.isVerifyRequest() = this is IdVerifyRequest
