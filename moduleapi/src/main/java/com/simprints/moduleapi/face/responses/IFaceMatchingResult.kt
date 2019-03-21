package com.simprints.moduleapi.face.responses

import android.os.Parcelable

interface IFaceMatchingResult : Parcelable {
    val guid: String
    val confidence: Int
    val tier: IFaceTier
}
