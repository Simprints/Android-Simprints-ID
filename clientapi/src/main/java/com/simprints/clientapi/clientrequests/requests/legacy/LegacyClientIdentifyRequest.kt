package com.simprints.clientapi.clientrequests.requests.legacy

import com.simprints.clientapi.clientrequests.requests.ApiVersion
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyIdentifyRequest


class LegacyClientIdentifyRequest(
    override val legacyApiKey: String,
    override val moduleId: String,
    override val userId: String,
    override val metadata: String?
) : LegacyClientRequest {

    override val apiVersion: ApiVersion = ApiVersion.V1
    override val projectId: String = ""

    override fun toSimprintsRequest(): SimprintsIdRequest = LegacyIdentifyRequest(
        legacyApiKey = legacyApiKey,
        userId = userId,
        moduleId = moduleId,
        metadata = metadata ?: ""
    )

}
