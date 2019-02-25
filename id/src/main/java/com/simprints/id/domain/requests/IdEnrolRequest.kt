package com.simprints.id.domain.requests

import com.simprints.clientapi.simprintsrequests.requests.ClientApiEnrollRequest

data class IdEnrolRequest(override val projectId: String,
                          override val userId: String,
                          override val moduleId: String,
                          override val metadata: String) : IdRequest {

    constructor(clientApiEnrolRequest: ClientApiEnrollRequest) : this(
        projectId = clientApiEnrolRequest.projectId,
        userId = clientApiEnrolRequest.userId,
        moduleId = clientApiEnrolRequest.moduleId,
        metadata = clientApiEnrolRequest.metadata
    )
}

fun ClientApiEnrollRequest.toDomainIdEnrolRequest() = IdEnrolRequest(this)
fun IdRequest.isEnrolRequest() = this is IdEnrolRequest
