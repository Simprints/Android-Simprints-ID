package com.simprints.moduleinterfaces.app.requests

import android.os.Parcelable


interface AppRequest : Parcelable {

    companion object {
        const val BUNDLE_KEY = "clientRequestBundleKey"
    }

    val projectId: String
    val userId: String
    val moduleId: String
    val metadata: String

}
