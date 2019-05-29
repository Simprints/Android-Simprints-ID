package com.simprints.fingerprint.data.domain.matching.result

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class MatchingActIdentifyResult(val identifications: List<MatchingResult>):
    MatchingActResult, Parcelable
