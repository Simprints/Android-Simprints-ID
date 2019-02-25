package com.simprints.clientapi.simprintsrequests.requests

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ClientApiConfirmIdentifyRequest(
    override val projectId: String,
    val sessionId: String,
    val selectedGuid: String
) : ClientApiBaseRequest, ClientApiConfirmationRequest {

    @IgnoredOnParcel
    override val bundleKey: String = BUNDLE_KEY

    companion object {
        const val BUNDLE_KEY = "confirmIdentifyRequest"
    }

}
