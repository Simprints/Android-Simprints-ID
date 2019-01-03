package com.simprints.clientapi.clientrequests.requests

import com.simprints.clientapi.clientrequests.requests.ApiVersion.V2
import com.simprints.clientapi.simprintsrequests.EnrollmentRequest
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest

class ClientEnrollmentRequest(
    override val projectId: String,
    override val moduleId: String,
    override val userId: String,
    override val metadata: String?
) : ClientRequest {

    override val apiVersion: ApiVersion = V2

    override fun toSimprintsRequest(): SimprintsIdRequest = EnrollmentRequest(
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        metadata = metadata ?: ""
    )

}



