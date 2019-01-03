package com.simprints.clientapi.clientrequests.requests

import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest
import com.simprints.clientapi.simprintsrequests.VerifyRequest


class ClientVerifyRequest(
    override val projectId: String,
    override val moduleId: String,
    override val userId: String,
    override val metadata: String?,
    val verifyGuid: String
) : ClientRequest {

    override val apiVersion: ApiVersion = ApiVersion.V2

    override fun toSimprintsRequest(): SimprintsIdRequest = VerifyRequest(
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        metadata = metadata ?: "",
        verifyGuid = verifyGuid
    )

}
