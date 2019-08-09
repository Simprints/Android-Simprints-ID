package com.simprints.moduleapi.face.requests

import android.os.Parcelable
import com.simprints.moduleapi.IRequest


interface IFaceRequest : Parcelable, IRequest {

    companion object {
        const val BUNDLE_KEY = "FaceRequestBundleKey"
    }

    val projectId: String
    val userId: String
    val moduleId: String
}
