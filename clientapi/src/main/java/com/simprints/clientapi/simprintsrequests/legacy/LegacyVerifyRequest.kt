package com.simprints.clientapi.simprintsrequests.legacy

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize


@Parcelize
data class LegacyVerifyRequest(override val legacyApiKey: String,
                               override val moduleId: String,
                               override val userId: String,
                               override val metadata: String,
                               val verifyGuid: String) : LegacySimprintsActionRequest {

    @IgnoredOnParcel
    override val requestName: String = REQUEST_NAME

    companion object {
        private const val REQUEST_NAME = "legacyVerifyRequest"
    }

}
