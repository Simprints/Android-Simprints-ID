package com.simprints.clientapi.simprintsrequests.requests

import android.os.Parcelable
import com.simprints.clientapi.simprintsrequests.ApiVersion
import kotlinx.android.parcel.IgnoredOnParcel

interface SimprintsIdRequest : Parcelable {

    @IgnoredOnParcel
    val requestName: String
    val projectId: String
    val apiVersion: ApiVersion get() = ApiVersion.V2

}

interface SimprintsActionRequest {

    val userId: String
    val moduleId: String
    val metadata: String

}

interface SimprintsConfirmationRequest

