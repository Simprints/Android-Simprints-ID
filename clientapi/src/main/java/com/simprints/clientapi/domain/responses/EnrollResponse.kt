package com.simprints.clientapi.domain.responses

import com.simprints.moduleapi.clientapi.responses.IClientApiEnrolResponse


data class EnrollResponse(val guid: String) {

    constructor(request: IClientApiEnrolResponse) : this(request.guid)

}
