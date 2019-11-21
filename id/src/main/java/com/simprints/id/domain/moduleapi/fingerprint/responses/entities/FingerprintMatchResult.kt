package com.simprints.id.domain.moduleapi.fingerprint.responses.entities

import android.os.Parcelable
import com.simprints.moduleapi.fingerprint.responses.entities.IFingerprintMatchResult
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintMatchResult(val personId: String,
                                  val confidenceScore: Float) : Parcelable

fun IFingerprintMatchResult.fromModuleApiToDomain() = FingerprintMatchResult(personId, confidenceScore)
