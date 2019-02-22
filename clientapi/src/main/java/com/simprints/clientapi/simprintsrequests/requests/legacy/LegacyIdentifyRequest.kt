package com.simprints.clientapi.simprintsrequests.requests.legacy

import com.simprints.clientapi.simprintsrequests.requests.SimprintsActionRequest
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize


@Parcelize
data class LegacyIdentifyRequest(
    override val legacyApiKey: String,
    override val moduleId: String,
    override val userId: String,
    override val metadata: String
) : LegacySimprintsIdRequest, SimprintsActionRequest {

    @IgnoredOnParcel
    override val requestName: String = REQUEST_NAME

    companion object {
        private const val REQUEST_NAME = "legacyIdentifyRequest"
    }
}
