package com.simprints.fingerprint.data.domain.matching.result

import android.os.Parcelable
import com.simprints.id.domain.matching.IdentificationResult
import kotlinx.android.parcel.Parcelize

@Parcelize
class MatchingIdentifyResult(val identifications: List<IdentificationResult>): MatchingResult, Parcelable
