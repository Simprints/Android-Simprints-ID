package com.simprints.id.domain.responses

import com.simprints.clientapi.simprintsrequests.responses.RefusalFormResponse

data class IdRefusalFormResponse(val reason: String, val extra: String): IdResponse

fun IdRefusalFormResponse.toDomainClientApiRefusalResponse() = RefusalFormResponse(reason, extra)
