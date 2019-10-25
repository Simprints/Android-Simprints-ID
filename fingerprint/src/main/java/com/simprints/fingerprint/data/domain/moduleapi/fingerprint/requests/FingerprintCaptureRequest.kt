package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests

import com.simprints.fingerprint.activities.collect.models.FingerIdentifier
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintCaptureRequest(val fingerprintsToCapture: List<FingerIdentifier>) : FingerprintRequest
