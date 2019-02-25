package com.simprints.clientapi.simprintsrequests.responses

import kotlinx.android.parcel.Parcelize


@Parcelize
data class RefusalFormResponse(val reason: String, val extra: String) : SimprintsIdResponse {

    companion object {
        const val BUNDLE_KEY = "refusalResponse"
    }

    override val bundleKey: String get() = BUNDLE_KEY

}
