package com.simprints.id.domain.responses

import com.simprints.id.domain.refusal_form.IdRefusalForm
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RefusalFormResponse(val reason: String, val extra: String): Response {

    constructor(refusalForm: IdRefusalForm): this(
        reason = refusalForm.reason ?: "",
        extra = refusalForm.extra ?: ""
    )
}

fun RefusalFormResponse.toDomainClientApiRefusalResponse() = ClientApiRefusalFormResponse(reason, extra)
