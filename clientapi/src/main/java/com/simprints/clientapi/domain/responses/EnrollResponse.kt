package com.simprints.clientapi.domain.responses

import com.simprints.moduleapi.app.responses.IAppEnrolResponse


data class EnrollResponse(val guid: String) {

    constructor(response: IAppEnrolResponse) : this(response.guid)

}
