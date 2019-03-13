package com.simprints.fingerprint.data.domain.requests

import com.simprints.moduleapi.fingerprint.IFingerprintEnrolRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintEnrolRequest(override val projectId: String,
                                   override val userId: String,
                                   override val moduleId: String,
                                   override val metadata: String) : FingerprintRequest {

    constructor(fingerprintRequest: IFingerprintEnrolRequest) : this(
        fingerprintRequest.projectId,
        fingerprintRequest.userId,
        fingerprintRequest.moduleId,
        fingerprintRequest.metadata)
}
