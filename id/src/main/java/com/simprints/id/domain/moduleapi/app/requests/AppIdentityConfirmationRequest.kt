package com.simprints.id.domain.moduleapi.app.requests

import com.simprints.moduleapi.app.requests.confirmations.IAppIdentifyConfirmation

data class AppIdentityConfirmationRequest(override val projectId: String,
                                          override val extraRequestInfo: AppExtraRequestInfo,
                                          val sessionId: String,
                                          val selectedGuid: String) : AppBaseRequest {

    constructor(appRequest: IAppIdentifyConfirmation) :
        this(appRequest.projectId,
            AppExtraRequestInfo(appRequest.extra),
            appRequest.sessionId,
            appRequest.selectedGuid)
}
