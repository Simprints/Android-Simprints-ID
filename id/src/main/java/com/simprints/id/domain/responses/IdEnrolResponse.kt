package com.simprints.id.domain.responses

import com.simprints.clientapi.simprintsrequests.responses.ClientApiEnrollResponse

data class IdEnrolResponse(val guid: String): IdResponse

fun IdEnrolResponse.toDomainClientApiEnrol() = ClientApiEnrollResponse(guid)
