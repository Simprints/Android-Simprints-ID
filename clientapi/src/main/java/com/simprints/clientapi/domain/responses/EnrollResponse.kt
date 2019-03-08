package com.simprints.clientapi.domain.responses

import com.simprints.moduleapi.clientapi.responses.IClientApiEnrollResponse


data class EnrollResponse(val guid: String) {

    constructor(request: IClientApiEnrollResponse) : this(request.guid)

}
