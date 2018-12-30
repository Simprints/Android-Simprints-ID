package com.simprints.clientapi.clientrequests.requests

import com.simprints.clientapi.clientrequests.requests.ApiVersion.V2
import com.simprints.clientapi.simprintsrequests.EnrollmentRequest
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest

class ClientEnrollmentRequest(
    projectId: String,
    moduleId: String,
    userId: String,
    metadata: String?
) : ClientRequest(
    projectId = projectId,
    moduleId = moduleId,
    userId = userId,
    metadata = metadata
) {

    override val apiVersion: ApiVersion = V2

    override fun toSimprintsRequest(): SimprintsIdRequest = EnrollmentRequest(
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        metadata = metadata ?: ""
    )

}



