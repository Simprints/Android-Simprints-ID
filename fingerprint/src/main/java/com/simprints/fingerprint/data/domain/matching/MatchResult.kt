package com.simprints.fingerprint.data.domain.matching

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MatchResult(
    val guid: String,
    val confidence: Float) : Parcelable
