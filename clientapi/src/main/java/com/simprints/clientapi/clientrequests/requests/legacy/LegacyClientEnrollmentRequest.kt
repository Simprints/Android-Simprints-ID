package com.simprints.clientapi.clientrequests.requests.legacy

import com.simprints.clientapi.clientrequests.requests.ApiVersion
import com.simprints.clientapi.clientrequests.requests.ApiVersion.*
import com.simprints.clientapi.clientrequests.requests.ClientRequest

class LegacyClientEnrollmentRequest(
    val apiKey: String,
    moduleId: String,
    userId: String,
    metadata: String?
) : ClientRequest(
    projectId = "",
    moduleId = moduleId,
    userId = userId,
    metadata = metadata
) {

    override val apiVersion: ApiVersion = V1

}
