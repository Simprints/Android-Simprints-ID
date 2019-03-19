package com.simprints.clientapi.domain.responses

import com.simprints.moduleapi.app.responses.IAppRefusalFormResponse


data class RefusalFormResponse(val reason: String,
                               val extra: String) {

    constructor(request: IAppRefusalFormResponse) : this(request.reason, request.extra)

}
