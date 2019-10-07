package com.simprints.id.domain.moduleapi.fingerprint.responses.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintMatchResult(val personId: String,
                                  val confidenceScore: Int) : Parcelable
