package com.simprints.id.domain.requests

import com.simprints.clientapi.simprintsrequests.requests.ClientApiEnrollRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AppEnrolRequest(override val projectId: String,
                          override val userId: String,
                          override val moduleId: String,
                          override val metadata: String) : AppRequest {

    constructor(clientApiEnrolRequest: ClientApiEnrollRequest) : this(
        projectId = clientApiEnrolRequest.projectId,
        userId = clientApiEnrolRequest.userId,
        moduleId = clientApiEnrolRequest.moduleId,
        metadata = clientApiEnrolRequest.metadata
    )
}

fun ClientApiEnrollRequest.toDomainIdEnrolRequest() = AppEnrolRequest(this)
fun AppRequest.isEnrolRequest() = this is AppEnrolRequest
