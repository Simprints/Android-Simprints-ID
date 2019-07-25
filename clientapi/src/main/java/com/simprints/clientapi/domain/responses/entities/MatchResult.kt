package com.simprints.clientapi.domain.responses.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MatchResult(val guidFound: String,
                       val confidence: Int,
                       val tier: Tier) : Parcelable
