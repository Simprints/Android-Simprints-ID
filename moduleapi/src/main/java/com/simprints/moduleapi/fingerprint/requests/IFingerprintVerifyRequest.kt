package com.simprints.moduleapi.fingerprint.requests


interface IFingerprintVerifyRequest : IFingerprintRequest {

    val verifyGuid: String
}
