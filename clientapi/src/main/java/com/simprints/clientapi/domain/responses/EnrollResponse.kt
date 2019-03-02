package com.simprints.clientapi.domain.responses

import com.simprints.moduleinterfaces.clientapi.responses.IClientApiEnrollResponse


data class EnrollResponse(val guid: String) {

    constructor(request: IClientApiEnrollResponse) : this(request.guid)

}
