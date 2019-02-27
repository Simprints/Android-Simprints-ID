package com.simprints.clientapi.simprintsrequests.responses

import android.os.Parcelable
import com.simprints.clientapi.simprintsrequests.ApiVersion


interface SimprintsIdResponse : Parcelable {

    val apiVersion: ApiVersion get() = ApiVersion.V2

    companion object {
        const val BUNDLE_KEY: String = "SimprintsIdResponseBundleName"
    }

}
