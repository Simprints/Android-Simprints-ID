package com.simprints.clientapi.simprintsrequests.responses

import com.simprints.moduleinterfaces.clientapi.responses.ClientIdentifyResponse
import com.simprints.moduleinterfaces.clientapi.responses.ClientIdentifyResponse.Identification
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ClientApiIdentifyResponse(override val identifications: List<Identification>,
                                     override val sessionId: String) : ClientIdentifyResponse
