package com.simprints.clientapi.models.domain.responses

import com.simprints.clientapi.models.appinterface.responses.AppRefusalFormResponse


data class RefusalFormResponse(val reason: String,
                               val extra: String) {

    constructor(request: AppRefusalFormResponse) : this(request.reason, request.extra)

}
