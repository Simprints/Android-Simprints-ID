package com.simprints.moduleapi.fingerprint.responses

interface IFingerprintMatchResponse: IFingerprintResponse {
    val result: List<IFingerprintMatchResult>
}
