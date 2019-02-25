package com.simprints.clientapi.simprintsrequests.requests

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ClientApiVerifyRequest(
    override val projectId: String,
    override val moduleId: String,
    override val userId: String,
    override val metadata: String,
    val verifyGuid: String
) : ClientApiBaseRequest, ClientApiActionRequest {

    @IgnoredOnParcel
    override val bundleKey: String = BUNDLE_KEY

    companion object {
        const val BUNDLE_KEY = "ClientApiVerifyRequest"
    }

}
