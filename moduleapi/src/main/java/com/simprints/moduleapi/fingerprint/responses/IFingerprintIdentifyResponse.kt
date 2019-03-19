package com.simprints.moduleapi.fingerprint.responses

import android.os.Parcelable


interface IFingerprintIdentifyResponse : IFingerprintResponse {

    val identifications: List<IIdentificationResult>
    val sessionId: String

    interface IIdentificationResult : Parcelable {
        val guid: String
        val confidence: Int
        val tier: IFingerprintResponseTier
    }
}
