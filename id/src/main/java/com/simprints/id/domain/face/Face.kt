package com.simprints.id.domain.face

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Face(val template: String = "",
                val yaw: String = "",
                val pitch: String = "") : Parcelable
