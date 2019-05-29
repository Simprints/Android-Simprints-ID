package com.simprints.clientapi.domain.responses

import com.simprints.moduleapi.app.responses.IAppRefusalFormResponse


data class RefusalFormResponse(val reason: String,
                               val extra: String) {

    constructor(response: IAppRefusalFormResponse) : this(response.reason, response.extra)

}
