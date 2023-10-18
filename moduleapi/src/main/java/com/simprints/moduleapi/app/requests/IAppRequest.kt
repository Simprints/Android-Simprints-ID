package com.simprints.moduleapi.app.requests

import android.os.Parcelable
import com.simprints.moduleapi.IRequest


interface IAppRequest : Parcelable, IRequest {

    companion object {
        const val BUNDLE_KEY = "clientRequestBundleKey"
    }

    val projectId: String
    val userId: String
    // Adding TokenizedString introduces circular dependency, and bringing TokenizedString
    // to this module introduces a lot of dependencies. Keeping it as a simple flag for now
    val isUserIdTokenized: Boolean
}
