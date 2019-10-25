package com.simprints.moduleapi.fingerprint.responses.entities

import android.os.Parcelable

interface IFingerprintMatchResult : Parcelable {
    val personId: String
    val confidenceScore: Float
}
