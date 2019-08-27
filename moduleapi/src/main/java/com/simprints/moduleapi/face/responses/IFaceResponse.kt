package com.simprints.moduleapi.face.responses

import android.os.Parcelable
import com.simprints.moduleapi.IResponse


interface IFaceResponse : Parcelable, IResponse {

    companion object {
        const val BUNDLE_KEY = "IFaceResponseBundleKey"
    }

}
