package com.simprints.id.domain.responses

import com.simprints.id.domain.matching.IdentificationResult
import com.simprints.id.domain.matching.toClientApiIdentificationResult
import com.simprints.moduleinterfaces.clientapi.responses.IClientApiIdentifyResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IdentifyResponse(val identifications: List<IdentificationResult>,
                            val sessionId: String): Response

fun IdentifyResponse.toClientApiIdentifyResponse(): IClientApiIdentifyResponse = ClientApiIdentifyResponse(
    this.identifications.map { it.toClientApiIdentificationResult() },
    sessionId
)

@Parcelize
private class ClientApiIdentifyResponse(override val identifications: List<IClientApiIdentifyResponse.IIdentificationResult>,
                                        override val sessionId: String): IClientApiIdentifyResponse


