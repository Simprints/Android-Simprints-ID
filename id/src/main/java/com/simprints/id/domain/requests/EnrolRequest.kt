package com.simprints.id.domain.requests

import com.simprints.moduleapi.app.requests.IAppEnrollRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EnrolRequest(override val projectId: String,
                        override val userId: String,
                        override val moduleId: String,
                        override val metadata: String) : Request {

    constructor(appRequest: IAppEnrollRequest) :
        this(appRequest.projectId, appRequest.userId, appRequest.moduleId, appRequest.metadata)
}

fun Request.isEnrolRequest() = this is EnrolRequest
