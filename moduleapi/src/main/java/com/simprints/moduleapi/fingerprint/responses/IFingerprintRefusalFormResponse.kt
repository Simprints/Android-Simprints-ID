package com.simprints.moduleapi.fingerprint.responses

interface IFingerprintRefusalFormResponse : IFingerprintResponse {

    val reason: String
    val extra: String

}
