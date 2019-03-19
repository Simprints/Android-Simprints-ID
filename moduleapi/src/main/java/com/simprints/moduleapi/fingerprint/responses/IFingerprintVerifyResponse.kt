package com.simprints.moduleapi.fingerprint.responses


interface IFingerprintVerifyResponse : IFingerprintResponse {

    val guid: String
    val confidence: Int
    val tier: IFingerprintResponseTier
}
