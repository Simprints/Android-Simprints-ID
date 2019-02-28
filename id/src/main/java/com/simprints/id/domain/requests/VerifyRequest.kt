package com.simprints.id.domain.requests

import com.simprints.moduleinterfaces.app.requests.IAppVerifyRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VerifyRequest(override val projectId: String,
                         override val userId: String,
                         override val moduleId: String,
                         override val metadata: String,
                         val verifyGuid: String) : Request {

    constructor(appRequest: IAppVerifyRequest) : this(
        appRequest.projectId,
        appRequest.userId,
        appRequest.moduleId,
        appRequest.metadata,
        appRequest.verifyGuid
    )
}

fun Request.isVerifyRequest() = this is VerifyRequest
