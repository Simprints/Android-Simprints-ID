package com.simprints.moduleapi.face.responses

import android.os.Parcelable
import com.simprints.moduleapi.IResponse


interface IFaceResponse : Parcelable, IResponse {

    val type: IFaceResponseType

    companion object {
        const val BUNDLE_KEY = "IFaceResponseBundleKey"
    }
}

enum class IFaceResponseType {
    CAPTURE,
    MATCH,
    EXIT_FORM,
    ERROR
}

