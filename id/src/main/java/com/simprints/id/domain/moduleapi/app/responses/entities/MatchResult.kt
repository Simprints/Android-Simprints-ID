package com.simprints.id.domain.moduleapi.app.responses.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MatchResult(val guidFound: String,
                       val confidence: Int,
                       val tier: Tier) : Parcelable
