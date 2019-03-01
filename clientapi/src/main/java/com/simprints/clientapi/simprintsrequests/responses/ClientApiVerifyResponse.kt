package com.simprints.clientapi.simprintsrequests.responses

import com.simprints.moduleinterfaces.clientapi.responses.IClientApiResponseTier
import com.simprints.moduleinterfaces.clientapi.responses.IClientApiVerifyResponse
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ClientApiVerifyResponse(override val guid: String,
                                   override val confidence: Int,
                                   override val tier: IClientApiResponseTier) : IClientApiVerifyResponse
