package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests

import com.simprints.fingerprint.activities.collect.models.FingerIdentifier
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintVerifyRequest(val projectId: String,
                                    val userId: String,
                                    val moduleId: String,
                                    val metadata: String,
                                    override val language: String,
                                    override val fingerStatus: Map<FingerIdentifier, Boolean>,
                                    val logoExists: Boolean,
                                    val programName: String,
                                    val organizationName: String,
                                    val verifyGuid: String) : FingerprintRequest
