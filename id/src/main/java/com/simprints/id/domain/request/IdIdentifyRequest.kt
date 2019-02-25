package com.simprints.id.domain.request

import com.simprints.clientapi.simprintsrequests.requests.ClientApiIdentifyRequest

class IdIdentifyRequest(override val projectId: String,
                        override val userId: String,
                        override val moduleId: String,
                        override val metadata: String) : IdRequest {

    constructor(clientApiVerifyRequest: ClientApiIdentifyRequest) : this(
        projectId = clientApiVerifyRequest.projectId,
        userId = clientApiVerifyRequest.userId,
        moduleId = clientApiVerifyRequest.moduleId,
        metadata = clientApiVerifyRequest.metadata
    )
}

fun ClientApiIdentifyRequest.toIdDomainIdIdentifyRequest() = IdIdentifyRequest(this)
fun IdRequest.isIdentifyRequest() = this is IdIdentifyRequest
