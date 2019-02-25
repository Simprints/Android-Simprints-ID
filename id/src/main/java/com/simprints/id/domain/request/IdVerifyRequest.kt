package com.simprints.id.domain.request

import com.simprints.clientapi.simprintsrequests.requests.ClientApiVerifyRequest

class IdVerifyRequest(projectId: String,
                     userId: String,
                     moduleId: String,
                     metadata: String,
                     val verifyGuid: String):
    IdBaseRequest(projectId, userId, moduleId, metadata) {

    constructor(clientApiVerifyRequest: ClientApiVerifyRequest) : this(
        projectId = clientApiVerifyRequest.projectId,
        userId = clientApiVerifyRequest.userId,
        moduleId = clientApiVerifyRequest.moduleId,
        metadata = clientApiVerifyRequest.metadata,
        verifyGuid = clientApiVerifyRequest.verifyGuid
    )
}

fun ClientApiVerifyRequest.toIdDomainIdVerifyRequest() = IdVerifyRequest(this)
fun IdBaseRequest.isVerifyRequest() = this is IdVerifyRequest
