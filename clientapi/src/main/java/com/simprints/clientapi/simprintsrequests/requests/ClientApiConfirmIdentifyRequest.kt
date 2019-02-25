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
    override val requestName: String = REQUEST_NAME

    companion object {
        private const val REQUEST_NAME = "confirmIdentifyRequest"
    }

}
