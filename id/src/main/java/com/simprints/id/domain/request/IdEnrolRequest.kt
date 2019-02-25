package com.simprints.id.domain.request

import com.simprints.clientapi.simprintsrequests.requests.ClientApiEnrollRequest

class IdEnrolRequest(override val projectId: String,
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

fun ClientApiEnrollRequest.toIdDomainEnrolRequest() = IdEnrolRequest(this)
fun IdRequest.isEnrolRequest() = this is IdEnrolRequest
