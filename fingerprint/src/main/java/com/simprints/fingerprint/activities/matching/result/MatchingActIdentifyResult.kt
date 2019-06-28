package com.simprints.fingerprint.activities.matching.result

import android.os.Parcelable
import com.simprints.fingerprint.data.domain.matching.MatchingResult
import kotlinx.android.parcel.Parcelize

@Parcelize
class MatchingActIdentifyResult(val identifications: List<MatchingResult>):
    MatchingActResult, Parcelable
