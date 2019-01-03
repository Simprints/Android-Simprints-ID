package com.simprints.clientapi.clientrequests.requests

import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest


class ClientVerifyRequest(
    override val projectId: String,
    override val moduleId: String,
    override val userId: String,
    override val metadata: String?,
    val verifyGuid: String
) : ClientRequest {

    override val apiVersion: ApiVersion = ApiVersion.V2

    override fun toSimprintsRequest(): SimprintsIdRequest {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
