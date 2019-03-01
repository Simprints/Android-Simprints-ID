package com.simprints.clientapi.simprintsrequests.responses

import com.simprints.moduleinterfaces.clientapi.responses.IClientApiIdentifyResponse
import com.simprints.moduleinterfaces.clientapi.responses.IClientApiIdentifyResponse.IIdentificationResult
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ClientApiIdentifyResponse(override val identifications: List<IIdentificationResult>,
                                     override val sessionId: String) : IClientApiIdentifyResponse
