package com.simprints.clientapi.domain.responses.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MatchResult(val guidFound: String,
                       val confidenceScore: Int,
                       val tier: Tier,
                       val matchConfidence: MatchConfidence) : Parcelable
