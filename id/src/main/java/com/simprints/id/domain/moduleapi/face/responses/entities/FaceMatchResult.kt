package com.simprints.id.domain.moduleapi.face.responses.entities

import android.os.Parcelable
import com.simprints.moduleapi.face.responses.entities.IFaceMatchingResult
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceMatchingResult(val guidFound: String,
                              val confidence: Int,
                              val tier: FaceTier) : Parcelable

fun IFaceMatchingResult.fromModuleApiToDomain() =
    FaceMatchingResult(guid, confidence, tier.fromDomainApiToDomain())
