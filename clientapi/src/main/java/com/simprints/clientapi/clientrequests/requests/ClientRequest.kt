package com.simprints.clientapi.clientrequests.requests

import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest


interface ClientRequest {

    val projectId: String
    val moduleId: String
    val userId: String
    val metadata: String?

    val apiVersion: ApiVersion

    fun toSimprintsRequest(): SimprintsIdRequest

}
