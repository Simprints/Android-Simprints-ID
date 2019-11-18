package com.simprints.face.data.moduleapi.face.responses.entities

import com.simprints.moduleapi.face.responses.IFaceMatchResult
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceMatchResult(override val guid: String,
                           override val confidence: Float) : IFaceMatchResult
