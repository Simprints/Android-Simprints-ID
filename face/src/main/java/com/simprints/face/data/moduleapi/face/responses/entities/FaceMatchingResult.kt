package com.simprints.face.data.moduleapi.face.responses.entities

import android.os.Parcelable
import com.simprints.id.domain.moduleapi.app.responses.entities.MatchResult
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceTier
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceMatchingResult(val guidFound: String,
                              val confidence: Int,
                              val tier: FaceTier) : Parcelable

fun FaceMatchingResult.toAppMatchResult() =
    MatchResult(this.guidFound, this.confidence, tier.toAppTier())
