package com.simprints.clientapi.domain.responses

import com.simprints.moduleapi.app.responses.IAppEnrolResponse


data class EnrollResponse(val guid: String) {

    constructor(request: IAppEnrolResponse) : this(request.guid)

}
