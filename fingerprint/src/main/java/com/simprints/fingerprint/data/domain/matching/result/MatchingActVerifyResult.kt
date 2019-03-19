package com.simprints.fingerprint.data.domain.matching.result

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class MatchingActVerifyResult(
    val guid: String,
    val confidence: Int,
    val tier: MatchingTier) : MatchingActResult, Parcelable
