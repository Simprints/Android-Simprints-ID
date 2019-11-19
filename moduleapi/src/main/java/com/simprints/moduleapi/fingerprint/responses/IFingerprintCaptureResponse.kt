package com.simprints.moduleapi.fingerprint.responses

import com.simprints.moduleapi.fingerprint.responses.entities.IFingerprintCaptureResult

interface IFingerprintCaptureResponse: IFingerprintResponse {
    val captureResult: List<IFingerprintCaptureResult>
}
