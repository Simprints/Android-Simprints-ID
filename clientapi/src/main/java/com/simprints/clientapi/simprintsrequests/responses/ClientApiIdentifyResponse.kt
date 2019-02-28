package com.simprints.clientapi.simprintsrequests.responses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ClientApiIdentifyResponse(val identifications: List<Identification>,
                                     val sessionId: String) : SimprintsIdResponse {

    @Parcelize
    data class Identification(val guid: String, val confidence: Int, val tier: ClientApiTier) : Parcelable

}
