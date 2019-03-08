package com.simprints.clientapi.domain.responses

import com.simprints.moduleapi.clientapi.responses.IClientApiRefusalFormResponse


data class RefusalFormResponse(val reason: String,
                               val extra: String) {

    constructor(request: IClientApiRefusalFormResponse) : this(request.reason, request.extra)

}
