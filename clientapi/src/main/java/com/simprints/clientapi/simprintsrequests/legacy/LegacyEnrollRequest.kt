package com.simprints.clientapi.simprintsrequests.legacy

import com.simprints.clientapi.simprintsrequests.SimprintsActionRequest
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize


@Parcelize
data class LegacyEnrollRequest(
    override val legacyApiKey: String,
    override val moduleId: String,
    override val userId: String,
    override val metadata: String
) : SimprintsActionRequest, LegacySimprintsIdRequest {

    @IgnoredOnParcel
    override val requestName: String = REQUEST_NAME

    companion object {
        private const val REQUEST_NAME = "legacyEnrollmentRequest"
    }
}
