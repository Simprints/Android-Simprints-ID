package com.simprints.clientapi.clientrequests.requests

import com.simprints.clientapi.simprintsrequests.IdentifyRequest
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest

class ClientIdentifyRequest(
    override val projectId: String,
    override val moduleId: String,
    override val userId: String,
    override val metadata: String?
) : ClientRequest {

    override val apiVersion: ApiVersion = ApiVersion.V2

    override fun toSimprintsRequest(): SimprintsIdRequest = IdentifyRequest(
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        metadata = metadata ?: ""
    )

}
