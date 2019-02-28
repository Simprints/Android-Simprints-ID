package com.simprints.clientapi.simprintsrequests.responses

import com.simprints.moduleinterfaces.clientapi.responses.ClientResponseTier
import com.simprints.moduleinterfaces.clientapi.responses.ClientVerifyResponse
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ClientApiVerifyResponse(override val guid: String,
                                   override val confidence: Int,
                                   override val tier: ClientResponseTier) : ClientVerifyResponse
