package com.simprints.moduleapi.face.responses

import android.os.Parcelable

interface IFaceMatchResult: Parcelable {
    val guid: String
    val confidence: Float
}
