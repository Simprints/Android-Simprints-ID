package com.simprints.fingerprint.data.domain.matching

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MatchingResult(
    val guid: String,
    val confidence: Int,
    val tier: MatchingTier) : Parcelable
