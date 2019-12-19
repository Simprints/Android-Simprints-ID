package com.simprints.id.domain.moduleapi.face.responses.entities

import android.os.Parcelable
import com.simprints.moduleapi.face.responses.IFaceMatchResult
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceMatchResult(val guidFound: String,
                           val confidence: Float) : Parcelable

fun IFaceMatchResult.fromModuleApiToDomain() =
    FaceMatchResult(guid, confidence)
