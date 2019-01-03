package com.simprints.clientapi.clientrequests.requests

import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest


class ClientVerifyRequest(
    projectId: String,
    moduleId: String,
    userId: String,
    metadata: String?,
    val verifyGuid: String
) : ClientRequest(
    projectId = projectId,
    moduleId = moduleId,
    userId = userId,
    metadata = metadata
) {

    override val apiVersion: ApiVersion = ApiVersion.V2

    override fun toSimprintsRequest(): SimprintsIdRequest {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
