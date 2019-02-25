package com.simprints.clientapi.simprintsrequests.responses

import android.os.Parcelable
import com.simprints.clientapi.simprintsrequests.ApiVersion
import kotlinx.android.parcel.IgnoredOnParcel


interface SimprintsIdResponse : Parcelable {

    val apiVersion: ApiVersion get() = ApiVersion.V2

    @IgnoredOnParcel
    val bundleKey: String

    companion object {
        const val BUNDLE_KEY: String = "SimprintsIdResponseBundleName"
    }

}
