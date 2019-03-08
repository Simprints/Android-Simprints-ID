package com.simprints.moduleapi.clientapi.responses

import android.os.Parcelable


interface IClientApiResponse : Parcelable {

    companion object {
        const val BUNDLE_KEY = "clientResponseBundleKey"
    }

}
