package com.simprints.id.domain.moduleapi.fingerprint.responses.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintMatchingResult(val guidFound: String,
                                     val confidence: Int,
                                     val tier: FingerprintTier) : Parcelable
