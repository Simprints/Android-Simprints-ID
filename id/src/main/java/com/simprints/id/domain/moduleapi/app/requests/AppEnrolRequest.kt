package com.simprints.id.domain.moduleapi.app.requests

import com.simprints.moduleapi.app.requests.IAppEnrollRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AppEnrolRequest(override val projectId: String,
                           override val userId: String,
                           override val moduleId: String,
                           override val metadata: String) : AppRequest {

    constructor(appRequest: IAppEnrollRequest) :
        this(appRequest.projectId, appRequest.userId, appRequest.moduleId, appRequest.metadata)
}
