package com.simprints.clientapi.simprintsrequests

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ConfirmIdentifyRequest(
    override val projectId: String,
    val sessionId: String,
    val selectedGuid: String
) : SimprintsIdRequest, SimprintsConfirmationRequest {

    @IgnoredOnParcel
    override val requestName: String = REQUEST_NAME

    companion object {
        private const val REQUEST_NAME = "confirmIdentifyRequest"
    }

}
