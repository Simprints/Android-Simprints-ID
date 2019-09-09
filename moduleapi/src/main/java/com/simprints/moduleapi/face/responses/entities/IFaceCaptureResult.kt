package com.simprints.moduleapi.face.responses.entities

import android.os.Parcelable

interface IFaceCaptureResult: Parcelable {
    val index: Int
    val sample: IFaceSample?
}
