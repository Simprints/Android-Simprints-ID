package com.simprints.fingerprint.activities.matching.result

import android.os.Parcelable
import com.simprints.fingerprint.data.domain.matching.MatchingTier
import kotlinx.android.parcel.Parcelize

@Parcelize
class MatchingTaskVerifyResult(
    val guid: String,
    val confidence: Int,
    val tier: MatchingTier) : MatchingTaskResult, Parcelable
