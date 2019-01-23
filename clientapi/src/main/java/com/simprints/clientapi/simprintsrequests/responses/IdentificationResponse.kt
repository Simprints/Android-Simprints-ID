package com.simprints.clientapi.simprintsrequests.responses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class IdentificationResponse(val identifications: List<Identification>) : SimprintsIdResponse {

    @Parcelize
    data class Identification(val guid: String, val confidence: Int, val tier: Tier) : Parcelable

}
