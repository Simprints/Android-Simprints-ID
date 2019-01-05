package com.simprints.clientapi.simprintsrequests.legacy

import com.simprints.clientapi.simprintsrequests.ApiVersion
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest


interface LegacySimprintsIdRequest : SimprintsIdRequest {

    override val apiVersion: ApiVersion get() = ApiVersion.V1
    override val projectId: String get() = ""
    val legacyApiKey: String

}

interface LegacySimprintsActionRequest : LegacySimprintsIdRequest {

    val userId: String
    val moduleId: String
    val metadata: String

}

interface SimprintsConfirmationRequest : LegacySimprintsIdRequest

