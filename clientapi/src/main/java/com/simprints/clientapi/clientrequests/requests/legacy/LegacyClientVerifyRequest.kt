package com.simprints.clientapi.clientrequests.requests.legacy

import com.simprints.clientapi.clientrequests.requests.ApiVersion
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyVerifyRequest


class LegacyClientVerifyRequest(
    override val legacyApiKey: String,
    override val moduleId: String,
    override val userId: String,
    override val metadata: String?,
    val verifyGuid: String
) : LegacyClientRequest {

    override val apiVersion: ApiVersion = ApiVersion.V1
    override val projectId: String = ""

    override fun toSimprintsRequest(): SimprintsIdRequest = LegacyVerifyRequest(
        legacyApiKey = legacyApiKey,
        userId = userId,
        moduleId = moduleId,
        metadata = metadata ?: "",
        verifyGuid = verifyGuid
    )

}
