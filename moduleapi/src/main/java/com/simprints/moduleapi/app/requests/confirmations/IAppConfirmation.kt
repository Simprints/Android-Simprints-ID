package com.simprints.moduleapi.app.requests.confirmations

import android.os.Parcelable
import com.simprints.moduleapi.app.requests.IExtraRequestInfo


interface IAppConfirmation : Parcelable {

    companion object {
        const val BUNDLE_KEY = "clientConfirmationBundleKey"
    }

    val projectId: String
    val extra: IExtraRequestInfo
}
