package com.simprints.id.domain.moduleapi.fingerprint.responses.entities

import android.os.Parcelable
import com.simprints.id.domain.moduleapi.app.responses.entities.MatchResult
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintMatchingResult(val guidFound: String,
                                     val confidence: Int,
                                     val tier: FingerprintTier) : Parcelable

fun FingerprintMatchingResult.toAppMatchResult() =
    MatchResult(this.guidFound, this.confidence, tier.toAppTier())
