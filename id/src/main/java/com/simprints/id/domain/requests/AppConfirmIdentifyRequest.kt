package com.simprints.id.domain.requests

import com.simprints.clientapi.simprintsrequests.requests.ClientApiConfirmIdentifyRequest

data class AppConfirmIdentifyRequest(override val projectId: String,
                                     val sessionId: String,
                                     val selectedGuid: String) : AppBaseRequest {

    constructor(clientApiConfirmIdentifyRequest: ClientApiConfirmIdentifyRequest) : this(
        projectId = clientApiConfirmIdentifyRequest.projectId,
        sessionId = clientApiConfirmIdentifyRequest.sessionId,
        selectedGuid = clientApiConfirmIdentifyRequest.selectedGuid
    )
}

fun ClientApiConfirmIdentifyRequest.toDomainIdConfirmIdentifyRequest() =
    AppConfirmIdentifyRequest(projectId, sessionId, selectedGuid)
