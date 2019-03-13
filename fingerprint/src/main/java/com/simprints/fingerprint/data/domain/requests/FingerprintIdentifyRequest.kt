package com.simprints.fingerprint.data.domain.requests

import com.simprints.moduleapi.fingerprint.IFingerprintIdentifyRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintIdentifyRequest(override val projectId: String,
                                      override val userId: String,
                                      override val moduleId: String,
                                      override val metadata: String) : FingerprintRequest {

    constructor(fingerprintRequest: IFingerprintIdentifyRequest) : this(
        fingerprintRequest.projectId,
        fingerprintRequest.userId,
        fingerprintRequest.moduleId,
        fingerprintRequest.metadata)
}
