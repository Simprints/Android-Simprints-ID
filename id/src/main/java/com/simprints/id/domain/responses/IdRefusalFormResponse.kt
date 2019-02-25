package com.simprints.id.domain.responses

import com.simprints.clientapi.simprintsrequests.responses.ClientApiRefusalFormResponse

data class IdRefusalFormResponse(val reason: String, val extra: String): IdResponse

fun IdRefusalFormResponse.toDomainClientApiRefusalResponse() = ClientApiRefusalFormResponse(reason, extra)
