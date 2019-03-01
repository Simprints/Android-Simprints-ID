package com.simprints.clientapi.models.domain.responses

import com.simprints.moduleinterfaces.clientapi.responses.IClientApiRefusalFormResponse


data class RefusalFormResponse(val reason: String,
                               val extra: String) {

    constructor(request: IClientApiRefusalFormResponse) : this(request.reason, request.extra)

}
