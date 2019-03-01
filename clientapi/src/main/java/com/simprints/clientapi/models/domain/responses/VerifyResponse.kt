package com.simprints.clientapi.models.domain.responses

import com.simprints.clientapi.models.appinterface.responses.AppVerifyResponse
import com.simprints.moduleinterfaces.clientapi.responses.IClientApiResponseTier
import com.simprints.moduleinterfaces.clientapi.responses.IClientApiVerifyResponse
import kotlinx.android.parcel.Parcelize


data class VerifyResponse(val guid: String,
                          val confidence: Int,
                          val tier: IClientApiResponseTier) {

    constructor(request: AppVerifyResponse) : this(request.guid, request.confidence, request.tier)

}
