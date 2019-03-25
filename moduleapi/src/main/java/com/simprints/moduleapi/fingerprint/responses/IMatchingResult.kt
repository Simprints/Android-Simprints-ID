package com.simprints.moduleapi.fingerprint.responses

import android.os.Parcelable

interface IMatchingResult : Parcelable {
    val guid: String
    val confidence: Int
    val tier: IFingerprintResponseTier
}
