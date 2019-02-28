package com.simprints.id.domain.matching

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IdentificationResult(val guidFound: String, val confidence: Int, val tier: Tier): Parcelable
