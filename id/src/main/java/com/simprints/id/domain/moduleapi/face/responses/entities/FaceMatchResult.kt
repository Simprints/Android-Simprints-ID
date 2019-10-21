package com.simprints.id.domain.moduleapi.face.responses.entities

import android.os.Parcelable
import com.simprints.moduleapi.face.responses.IFaceMatchResult
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceMatchResult(val guidFound: String,
                           val confidence: Int,
                           val tier: FaceTier) : Parcelable

fun IFaceMatchResult.fromModuleApiToDomain() =
    FaceMatchResult(guid, confidence, tier.fromDomainApiToDomain())
