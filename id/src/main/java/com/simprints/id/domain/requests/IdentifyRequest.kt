package com.simprints.id.domain.requests

import com.simprints.moduleapi.app.requests.IAppIdentifyRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IdentifyRequest(override val projectId: String,
                           override val userId: String,
                           override val moduleId: String,
                           override val metadata: String) : Request {

    constructor(appRequest: IAppIdentifyRequest) : this(
        appRequest.projectId,
        appRequest.userId,
        appRequest.moduleId,
        appRequest.metadata)
}

fun Request.isIdentifyRequest() = this is IdentifyRequest
