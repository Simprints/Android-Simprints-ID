package com.simprints.moduleapi.fingerprint.requests

import com.simprints.moduleapi.fingerprint.IFingerIdentifier

interface IFingerprintCaptureRequest: IFingerprintRequest {
    val fingerprintsToCapture: List<IFingerIdentifier>
}
