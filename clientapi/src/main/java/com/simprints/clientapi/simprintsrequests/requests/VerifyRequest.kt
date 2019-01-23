package com.simprints.clientapi.simprintsrequests.requests

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VerifyRequest(
    override val projectId: String,
    override val moduleId: String,
    override val userId: String,
    override val metadata: String,
    val verifyGuid: String
) : SimprintsIdRequest, SimprintsActionRequest {

    @IgnoredOnParcel
    override val requestName: String = REQUEST_NAME

    companion object {
        const val REQUEST_NAME = "VerifyRequest"
    }

}
