package com.simprints.id.domain.moduleapi.fingerprint.requests

import com.simprints.id.domain.moduleapi.fingerprint.requests.entities.FingerprintFingerIdentifier
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintEnrolRequest(val projectId: String,
                                   val userId: String,
                                   val moduleId: String,
                                   val metadata: String,
                                   val language: String,
                                   val fingerStatus: Map<FingerprintFingerIdentifier, Boolean>,
                                   val nudgeMode: Boolean,
                                   val qualityThreshold: Int,
                                   val logoExists: Boolean,
                                   val programName: String,
                                   val organizationName: String,
                                   val vibrateMode: Boolean): FingerprintRequest
