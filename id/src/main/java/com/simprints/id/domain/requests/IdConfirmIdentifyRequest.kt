package com.simprints.id.domain.requests

import com.simprints.clientapi.simprintsrequests.requests.ClientApiConfirmIdentifyRequest

data class IdConfirmIdentifyRequest(override val projectId: String,
                                    val sessionId: String,
                                    val selectedGuid: String): IdBaseRequest {

    constructor(clientApiConfirmIdentifyRequest: ClientApiConfirmIdentifyRequest) : this(
        projectId = clientApiConfirmIdentifyRequest.projectId,
        sessionId = clientApiConfirmIdentifyRequest.sessionId,
        selectedGuid = clientApiConfirmIdentifyRequest.selectedGuid
    )
}
fun ClientApiConfirmIdentifyRequest.toDomainIdConfirmIdentifyRequest() =
    IdConfirmIdentifyRequest(projectId, sessionId, selectedGuid)
