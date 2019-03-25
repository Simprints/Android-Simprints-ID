package com.simprints.face.data.moduleapi.face.responses.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceMatchingResult(val guidFound: String,
                              val confidence: Int,
                              val tier: FaceTier) : Parcelable
