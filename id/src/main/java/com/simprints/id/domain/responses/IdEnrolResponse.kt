package com.simprints.id.domain.responses

import com.simprints.clientapi.simprintsrequests.responses.EnrollResponse

data class IdEnrolResponse(val guid: String): IdResponse

fun IdEnrolResponse.toDomainClientApiEnrol() = EnrollResponse(guid)
