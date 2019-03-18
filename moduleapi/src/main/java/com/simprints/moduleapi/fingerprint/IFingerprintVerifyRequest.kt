package com.simprints.moduleapi.fingerprint


interface IFingerprintVerifyRequest : IFingerprintRequest {

    val verifyGuid: String
}
