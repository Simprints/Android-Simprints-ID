package com.simprints.clientapi.simprintsrequests

import android.os.Parcelable

interface SimprintsIdRequest : Parcelable {

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

