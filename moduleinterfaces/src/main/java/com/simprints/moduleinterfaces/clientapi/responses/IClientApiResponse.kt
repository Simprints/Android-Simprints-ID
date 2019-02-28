package com.simprints.moduleinterfaces.clientapi.responses

import android.os.Parcelable


interface IClientApiResponse : Parcelable {

    companion object {
        const val BUNDLE_KEY = "clientResponseBundleKey"
    }

}
