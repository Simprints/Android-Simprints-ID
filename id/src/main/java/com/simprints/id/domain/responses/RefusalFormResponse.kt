package com.simprints.id.domain.responses

import com.simprints.moduleinterfaces.clientapi.responses.IClientApiRefusalFormResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RefusalFormResponse(val reason: String, val extra: String): Response

fun RefusalFormResponse.toClientApiIRefusalFormResponse(): IClientApiRefusalFormResponse =
    ClientApiRefusalFormResponse(reason, extra)

@Parcelize
private class ClientApiRefusalFormResponse(
    override val reason: String,
    override val extra: String): IClientApiRefusalFormResponse


