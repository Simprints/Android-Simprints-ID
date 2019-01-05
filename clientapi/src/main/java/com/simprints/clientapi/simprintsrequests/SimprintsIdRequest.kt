package com.simprints.clientapi.simprintsrequests

import android.os.Parcelable

interface SimprintsIdRequest : Parcelable {

    val apiVersion: ApiVersion
        get() = ApiVersion.V2

    val requestName: String

    val projectId: String
    val userId: String
    val moduleId: String
    val metadata: String

}
