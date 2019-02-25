package com.simprints.clientapi.simprintsrequests.responses

import kotlinx.android.parcel.Parcelize


@Parcelize
data class ClientApiVerifyResponse(val guid: String, val confidence: Int, val tier: ClientApiTier) : SimprintsIdResponse {

    companion object {
        const val BUNDLE_KEY = "verificationResponse"
    }

    override val bundleKey: String get() = BUNDLE_KEY

}
