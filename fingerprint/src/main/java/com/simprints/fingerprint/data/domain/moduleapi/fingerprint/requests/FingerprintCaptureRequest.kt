package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests

import com.simprints.fingerprint.activities.collect.models.FingerIdentifier
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintCaptureRequest(override val language: String,
                                     override val fingerStatus: Map<FingerIdentifier, Boolean>,
                                     val activityTitle: String) : FingerprintRequest
