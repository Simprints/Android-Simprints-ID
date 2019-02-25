package com.simprints.id.domain.request

import com.simprints.clientapi.simprintsrequests.requests.ClientApiIdentifyRequest

class IdIdentifyRequest(projectId: String,
                        userId: String,
                        moduleId: String,
                        metadata: String) :
    IdBaseRequest(projectId, userId, moduleId, metadata) {

    constructor(clientApiVerifyRequest: ClientApiIdentifyRequest) : this(
        projectId = clientApiVerifyRequest.projectId,
        userId = clientApiVerifyRequest.userId,
        moduleId = clientApiVerifyRequest.moduleId,
        metadata = clientApiVerifyRequest.metadata
    )
}
