package com.simprints.moduleapi.fingerprint.responses

interface IFingerprintVerifyResponse : IFingerprintResponse {

    val matchingResult: IMatchingResult
}
