package com.simprints.face.data.moduleapi.face.responses

import android.os.Parcelable

interface FaceResponse : Parcelable {
    val type: FaceResponseType
}

enum class FaceResponseType {
    CAPTURE,
    MATCH
}
