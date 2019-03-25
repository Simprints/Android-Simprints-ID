package com.simprints.moduleapi.app.requests.confirmations

import android.os.Parcelable


interface IAppConfirmation : Parcelable {

    companion object {
        const val BUNDLE_KEY = "clientConfirmationBundleKey"
    }

    val projectId: String

}
