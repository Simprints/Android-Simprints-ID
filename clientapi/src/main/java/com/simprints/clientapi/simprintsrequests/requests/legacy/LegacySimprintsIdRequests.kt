package com.simprints.clientapi.simprintsrequests.requests.legacy

import com.simprints.clientapi.simprintsrequests.ApiVersion
import com.simprints.clientapi.simprintsrequests.requests.SimprintsIdRequest


interface LegacySimprintsIdRequest : SimprintsIdRequest {

    val legacyApiKey: String

    override val apiVersion: ApiVersion get() = ApiVersion.V1
    override val projectId: String get() = ""

}


