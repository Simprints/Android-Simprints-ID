package com.simprints.id.domain.responses

import com.simprints.id.domain.refusal_form.RefusalFormAnswer
import com.simprints.moduleinterfaces.clientapi.responses.IClientApiRefusalFormResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RefusalFormResponse(val answer: RefusalFormAnswer): Response

fun RefusalFormResponse.toClientApiRefusalFormResponse(): IClientApiRefusalFormResponse =
    ClientApiRefusalFormResponse(answer.reason.toString(), answer.optionalText)

@Parcelize
private class ClientApiRefusalFormResponse(
    override val reason: String,
    override val extra: String): IClientApiRefusalFormResponse


