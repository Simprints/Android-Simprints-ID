package com.simprints.moduleapi.app.requests

import android.os.Parcelable
import com.simprints.moduleapi.IRequest


interface IAppRequest : Parcelable, IRequest {

    companion object {
        const val BUNDLE_KEY = "clientRequestBundleKey"
    }

    val projectId: String
    val userId: String
    val moduleId: String
    val metadata: String
}
