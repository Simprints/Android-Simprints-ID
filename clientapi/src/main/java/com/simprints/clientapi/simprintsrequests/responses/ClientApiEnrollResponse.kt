package com.simprints.clientapi.simprintsrequests.responses

import kotlinx.android.parcel.Parcelize

@Parcelize
data class ClientApiEnrollResponse(val guid: String) : SimprintsIdResponse {

    companion object {
        const val BUNDLE_KEY = "enrollmentResponse"
    }

    override val bundleKey: String get() = BUNDLE_KEY

}

