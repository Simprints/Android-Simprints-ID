package com.simprints.clientapi.clientrequests.requests

import com.simprints.clientapi.clientrequests.requests.ApiVersion.*

class ClientEnrollmentRequest(
    projectId: String?,
    apiKey: String?,
    moduleId: String,
    userId: String,
    metadata: String?
) : ClientRequest(
    projectId = projectId,
    apiKey = apiKey,
    moduleId = moduleId,
    userId = userId,
    metadata = metadata
) {

    override val apiVersion: ApiVersion = V2

}
