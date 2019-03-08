package com.simprints.id.domain.requests

import com.simprints.moduleapi.app.confirmations.IAppIdentifyConfirmation

data class IdentityConfirmationRequest(override val projectId: String,
                                       val sessionId: String,
                                       val selectedGuid: String) : BaseRequest {

    constructor(appRequest: IAppIdentifyConfirmation) : this(appRequest.projectId, appRequest.sessionId, appRequest.selectedGuid)
}

fun IAppIdentifyConfirmation.toDomainIdentityConfirmationRequest() =
    IdentityConfirmationRequest(projectId, sessionId, selectedGuid)
