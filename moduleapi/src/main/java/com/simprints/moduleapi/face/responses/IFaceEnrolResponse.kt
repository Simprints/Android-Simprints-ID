package com.simprints.moduleapi.face.responses

import android.os.Parcelable

interface IFaceEnrolResponse : Parcelable, IFaceResponse {
    val guid: String
}
