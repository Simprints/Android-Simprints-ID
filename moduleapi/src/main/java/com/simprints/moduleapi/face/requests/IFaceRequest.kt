package com.simprints.moduleapi.face.requests

import android.os.Parcelable
import com.simprints.moduleapi.IRequest


interface IFaceRequest : Parcelable, IRequest {

    val type: IFaceRequestType

    companion object {
        const val BUNDLE_KEY = "FaceRequestBundleKey"
    }
}

enum class IFaceRequestType {
    CAPTURE,
    VERIFY,
    IDENTIFY
}
