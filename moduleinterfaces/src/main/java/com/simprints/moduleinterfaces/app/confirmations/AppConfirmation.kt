package com.simprints.moduleinterfaces.app.confirmations

import android.os.Parcelable


interface AppConfirmation : Parcelable {

    companion object {
        const val BUNDLE_KEY = "clientConfirmationBundleKey"
    }

    val projectId: String

}
