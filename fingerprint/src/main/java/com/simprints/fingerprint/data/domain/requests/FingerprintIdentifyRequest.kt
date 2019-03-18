package com.simprints.fingerprint.data.domain.requests

import com.simprints.fingerprint.activities.collect.models.FingerIdentifier
import com.simprints.fingerprint.activities.collect.models.toDomainClass
import com.simprints.moduleapi.fingerprint.IFingerprintIdentifyRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintIdentifyRequest(override val projectId: String,
                                      override val userId: String,
                                      override val moduleId: String,
                                      override val metadata: String,
                                      override val language: String,
                                      override val fingerStatus: Map<FingerIdentifier, Boolean>,
                                      override val nudgeMode: Boolean,
                                      override val qualityThreshold: Int) : FingerprintRequest {

    constructor(fingerprintRequest: IFingerprintIdentifyRequest) : this(
        fingerprintRequest.projectId,
        fingerprintRequest.userId,
        fingerprintRequest.moduleId,
        fingerprintRequest.metadata,
        fingerprintRequest.language,
        fingerprintRequest.fingerStatus.mapKeys { it.key.toDomainClass() },
        fingerprintRequest.nudgeMode,
        fingerprintRequest.qualityThreshold)
}
