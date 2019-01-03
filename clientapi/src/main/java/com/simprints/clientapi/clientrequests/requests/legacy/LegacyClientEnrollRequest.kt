package com.simprints.clientapi.clientrequests.requests.legacy

import com.simprints.clientapi.clientrequests.requests.ApiVersion
import com.simprints.clientapi.clientrequests.requests.ApiVersion.V1
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyEnrollRequest

class LegacyClientEnrollRequest(
    override val legacyApiKey: String,
    override val moduleId: String,
    override val userId: String,
    override val metadata: String?
) : LegacyClientRequest {

    override val apiVersion: ApiVersion = V1
    override val projectId: String = ""

    override fun toSimprintsRequest(): SimprintsIdRequest = LegacyEnrollRequest(
        legacyApiKey = legacyApiKey,
        userId = userId,
        moduleId = moduleId,
        metadata = metadata ?: ""
    )

}
