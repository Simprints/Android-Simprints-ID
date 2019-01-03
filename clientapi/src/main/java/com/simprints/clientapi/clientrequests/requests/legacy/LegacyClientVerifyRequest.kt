package com.simprints.clientapi.clientrequests.requests.legacy

import com.simprints.clientapi.clientrequests.requests.ApiVersion
import com.simprints.clientapi.clientrequests.requests.ClientRequest
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest


class LegacyClientVerifyRequest(
    val apiKey: String,
    moduleId: String,
    userId: String,
    metadata: String?,
    val verifyGuid: String
) : ClientRequest(
    projectId = "",
    moduleId = moduleId,
    userId = userId,
    metadata = metadata
) {

    override val apiVersion: ApiVersion = ApiVersion.V1

    override fun toSimprintsRequest(): SimprintsIdRequest {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
