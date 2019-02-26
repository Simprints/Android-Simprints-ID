package com.simprints.id.domain.responses

import com.simprints.clientapi.simprintsrequests.responses.ClientApiRefusalFormResponse
import com.simprints.id.domain.refusal_form.IdRefusalForm

data class IdRefusalFormResponse(val reason: String, val extra: String): IdResponse {

    constructor(refusalForm: IdRefusalForm): this(
        reason = refusalForm.reason ?: "",
        extra = refusalForm.extra ?: ""
    )
}

fun IdRefusalFormResponse.toDomainClientApiRefusalResponse() = ClientApiRefusalFormResponse(reason, extra)
