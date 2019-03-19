package com.simprints.moduleapi.app.responses

import android.os.Parcelable


interface IAppResponse : Parcelable {

    companion object {
        const val BUNDLE_KEY = "clientResponseBundleKey"
    }

}
