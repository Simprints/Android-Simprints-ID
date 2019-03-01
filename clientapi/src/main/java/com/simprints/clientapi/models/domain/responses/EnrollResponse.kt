package com.simprints.clientapi.models.domain.responses

import com.simprints.clientapi.models.appinterface.responses.AppEnrollResponse


data class EnrollResponse(val guid: String) {

    constructor(request: AppEnrollResponse) : this(request.guid)

}
