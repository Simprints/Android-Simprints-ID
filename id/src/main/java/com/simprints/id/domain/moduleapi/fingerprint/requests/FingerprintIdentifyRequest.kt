package com.simprints.id.domain.moduleapi.fingerprint.requests

import com.simprints.id.domain.moduleapi.fingerprint.requests.entities.FingerprintFingerIdentifier
import com.simprints.id.domain.moduleapi.fingerprint.requests.entities.FingerprintMatchGroup
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintIdentifyRequest(val projectId: String,
                                      val userId: String,
                                      val moduleId: String,
                                      val metadata: String,
                                      val language: String,
                                      val fingerStatus: Map<FingerprintFingerIdentifier, Boolean>,
                                      val logoExists: Boolean,
                                      val programName: String,
                                      val organizationName: String,
                                      val matchGroup: FingerprintMatchGroup,
                                      val returnIdCount: Int) : FingerprintRequest

