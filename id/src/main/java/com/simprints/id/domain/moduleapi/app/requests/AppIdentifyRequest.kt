package com.simprints.id.domain.moduleapi.app.requests

import com.simprints.moduleapi.app.requests.IAppIdentifyRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AppIdentifyRequest(override val projectId: String,
                              override val userId: String,
                              override val moduleId: String,
                              override val metadata: String) : AppRequest {

    constructor(appRequest: IAppIdentifyRequest) : this(
        appRequest.projectId,
        appRequest.userId,
        appRequest.moduleId,
        appRequest.metadata)
}

fun AppRequest.isIdentifyRequest() = this is AppIdentifyRequest
