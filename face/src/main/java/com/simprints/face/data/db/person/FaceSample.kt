package com.simprints.face.data.db.person

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceSample(val faceId: String, val template: ByteArray) : Parcelable
