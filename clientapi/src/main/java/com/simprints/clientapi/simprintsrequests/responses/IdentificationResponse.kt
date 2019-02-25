package com.simprints.clientapi.simprintsrequests.responses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class IdentificationResponse(val identifications: List<Identification>,
                                  val sessionId: String) : SimprintsIdResponse {

    companion object {
        const val BUNDLE_KEY = "identificationResponse"
    }

    override val bundleKey: String get() = BUNDLE_KEY

    @Parcelize
    data class Identification(val guid: String, val confidence: Int, val tier: Tier) : Parcelable

}
