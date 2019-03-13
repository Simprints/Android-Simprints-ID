package com.simprints.fingerprint.data.domain.requests

import com.simprints.moduleapi.fingerprint.IFingerprintVerifyRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintVerifyRequest(override val projectId: String,
                                    override val userId: String,
                                    override val moduleId: String,
                                    override val metadata: String,
                                    val verifyGuid: String) : FingerprintRequest {
    constructor(fingerprintRequest: IFingerprintVerifyRequest) : this(
        fingerprintRequest.projectId,
        fingerprintRequest.userId,
        fingerprintRequest.moduleId,
        fingerprintRequest.metadata,
        fingerprintRequest.verifyGuid)
}
