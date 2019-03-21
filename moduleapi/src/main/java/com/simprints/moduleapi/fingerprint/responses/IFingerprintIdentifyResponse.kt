package com.simprints.moduleapi.fingerprint.responses

interface IFingerprintIdentifyResponse : IFingerprintResponse {

    val identifications: List<IMatchingResult>
}
