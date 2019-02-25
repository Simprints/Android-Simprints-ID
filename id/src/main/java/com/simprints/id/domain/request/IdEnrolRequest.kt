package com.simprints.id.domain.request

import com.simprints.clientapi.simprintsrequests.requests.ClientApiEnrollRequest

class IdEnrolRequest(projectId: String,
                     userId: String,
                     moduleId: String,
                     metadata: String) :
    IdBaseRequest(projectId, userId, moduleId, metadata) {

    constructor(clientApiEnrolRequest: ClientApiEnrollRequest) : this(
        projectId = clientApiEnrolRequest.projectId,
        userId = clientApiEnrolRequest.userId,
        moduleId = clientApiEnrolRequest.moduleId,
        metadata = clientApiEnrolRequest.metadata
    )
}

fun ClientApiEnrollRequest.toIdDomainEnrolRequest() = IdEnrolRequest(this)
fun IdBaseRequest.isEnrolRequest() = this is IdEnrolRequest
