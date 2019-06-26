package com.simprints.id.domain.moduleapi.app.requests

import com.google.gson.Gson
import com.simprints.moduleapi.app.requests.confirmations.IAppIdentifyConfirmation

data class AppIdentityConfirmationRequest(override val projectId: String,
                                          val sessionId: String,
                                          val selectedGuid: String) : AppBaseRequest {

    constructor(appRequest: IAppIdentifyConfirmation) :
        this(appRequest.projectId,
            appRequest.sessionId,
            appRequest.selectedGuid)

    fun toJson(): String = Gson().toJson(this)

}
