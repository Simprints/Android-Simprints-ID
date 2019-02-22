package com.simprints.clientapi.simprintsrequests.requests.legacy

import com.simprints.clientapi.simprintsrequests.requests.SimprintsConfirmationRequest
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize


@Parcelize
data class LegacyConfirmIdentifyRequest(
    override val legacyApiKey: String,
    val sessionId: String,
    val selectedGuid: String
) : LegacySimprintsIdRequest, SimprintsConfirmationRequest {

    @IgnoredOnParcel
    override val requestName: String = REQUEST_NAME

    companion object {
        private const val REQUEST_NAME = "legacyConfirmIdentify"
    }
}
