package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests

import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import kotlinx.parcelize.Parcelize

/**
 * This class represents the request used to capture fingerprints, containing the list of fingers
 * to be captured.
 *
 * @param fingerprintsToCapture the list of fingerprints to be captured
 */
@Parcelize
data class FingerprintCaptureRequest(val fingerprintsToCapture: List<FingerIdentifier>) : FingerprintRequest
