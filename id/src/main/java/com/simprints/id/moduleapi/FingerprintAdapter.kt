package com.simprints.id.moduleapi

import com.simprints.id.domain.requests.EnrolRequest
import com.simprints.id.domain.requests.IdentifyRequest
import com.simprints.id.domain.requests.Request
import com.simprints.id.domain.requests.VerifyRequest
import com.simprints.moduleapi.fingerprint.IFingerprintEnrolRequest
import com.simprints.moduleapi.fingerprint.IFingerprintIdentifyRequest
import com.simprints.moduleapi.fingerprint.IFingerprintRequest
import com.simprints.moduleapi.fingerprint.IFingerprintVerifyRequest
import kotlinx.android.parcel.Parcelize

object FingerprintAdapter {

    fun toFingerprintRequest(appRequest: Request): IFingerprintRequest =
        when(appRequest) {
            is EnrolRequest -> toFingerprintEnrolRequest(appRequest)
            is VerifyRequest -> toFingerprintVerifyRequest(appRequest)
            is IdentifyRequest -> toFingerprintIdentifyRequest(appRequest)
            else -> throw IllegalStateException("Invalid fingerprint request")
        }

    private fun toFingerprintEnrolRequest(enrol: EnrolRequest): IFingerprintEnrolRequest =
        with(enrol) { FingerprintEnrollRequest(projectId, userId, moduleId, metadata) }

    private fun toFingerprintVerifyRequest(verify: VerifyRequest): IFingerprintVerifyRequest =
        with(verify) { FingerprintVerifyRequest(projectId, userId, moduleId, metadata, verifyGuid) }

    private fun toFingerprintIdentifyRequest(identify: IdentifyRequest): IFingerprintIdentifyRequest =
        with(identify) { FingerprintIdentifyRequest(projectId, userId, moduleId, metadata) }
}

@Parcelize
private data class FingerprintEnrollRequest(override val projectId: String,
                                            override val userId: String,
                                            override val moduleId: String,
                                            override val metadata: String) : IFingerprintEnrolRequest

@Parcelize
private data class FingerprintIdentifyRequest(override val projectId: String,
                                              override val userId: String,
                                              override val moduleId: String,
                                              override val metadata: String) : IFingerprintIdentifyRequest

@Parcelize
private data class FingerprintVerifyRequest(override val projectId: String,
                                            override val userId: String,
                                            override val moduleId: String,
                                            override val metadata: String,
                                            override val verifyGuid: String) : IFingerprintVerifyRequest
