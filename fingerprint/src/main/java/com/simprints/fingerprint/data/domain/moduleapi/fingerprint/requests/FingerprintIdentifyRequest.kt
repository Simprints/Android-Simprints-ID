package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests

import com.simprints.fingerprint.activities.collect.models.FingerIdentifier
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintIdentifyRequest(override val projectId: String,
                                      override val userId: String,
                                      override val moduleId: String,
                                      override val metadata: String,
                                      override val language: String,
                                      override val fingerStatus: Map<FingerIdentifier, Boolean>,
                                      override val logoExists: Boolean,
                                      override val organizationName: String,
                                      override val programName: String,
                                      val matchGroup: MatchGroup,
                                      val returnIdCount: Int) : FingerprintRequest
