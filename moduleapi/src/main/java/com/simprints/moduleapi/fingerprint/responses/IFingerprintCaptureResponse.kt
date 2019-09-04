package com.simprints.moduleapi.fingerprint.responses

interface IFingerprintCaptureResponse: IFingerprintResponse {

    val fingerprints: List<IFingerprint>
}
