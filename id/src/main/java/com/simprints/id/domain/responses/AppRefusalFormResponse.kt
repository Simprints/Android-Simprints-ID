package com.simprints.id.domain.responses

import com.simprints.clientapi.simprintsrequests.responses.ClientApiRefusalFormResponse
import com.simprints.id.domain.refusal_form.IdRefusalForm
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AppRefusalFormResponse(val reason: String, val extra: String): AppResponse {

    constructor(refusalForm: IdRefusalForm): this(
        reason = refusalForm.reason ?: "",
        extra = refusalForm.extra ?: ""
    )
}

fun AppRefusalFormResponse.toDomainClientApiRefusalResponse() = ClientApiRefusalFormResponse(reason, extra)
