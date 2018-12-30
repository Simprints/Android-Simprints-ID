package com.simprints.clientapi.clientrequests.requests.legacy

import com.simprints.clientapi.clientrequests.requests.ApiVersion
import com.simprints.clientapi.clientrequests.requests.ApiVersion.V1
import com.simprints.clientapi.clientrequests.requests.ClientRequest
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyEnrollmentRequest

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

    override fun toSimprintsRequest(): SimprintsIdRequest = LegacyEnrollmentRequest(
        legacyApiKey = apiKey,
        userId = userId,
        moduleId = moduleId,
        metadata = metadata ?: ""
    )

}
