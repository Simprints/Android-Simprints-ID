package com.simprints.id.domain.moduleapi.app.requests

import com.simprints.moduleapi.app.requests.IAppVerifyRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AppVerifyRequest(override val projectId: String,
                            override val userId: String,
                            override val moduleId: String,
                            override val metadata: String,
                            val verifyGuid: String) : AppRequest {
    override val type: AppRequestType
        get() = AppRequestType.VERIFY

    constructor(appRequest: IAppVerifyRequest) : this(
        appRequest.projectId,
        appRequest.userId,
        appRequest.moduleId,
        appRequest.metadata,
        appRequest.verifyGuid
    )
}
