package com.simprints.fingerprint.data.domain.moduleapi.fingerprint

import com.simprints.fingerprint.activities.collect.models.toDomainClass
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.*
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForFingerprintException
import com.simprints.moduleapi.fingerprint.requests.*

object FingerprintToDomainRequest {

    fun fromFingerprintToDomainRequest(iFingerprintRequest: IFingerprintRequest): FingerprintRequest =
        when (iFingerprintRequest) {
            is IFingerprintCaptureRequest ->
                fromFingerprintToDomainCaptureRequest(iFingerprintRequest)
            is IFingerprintVerifyRequest ->
                fromFingerprintToDomainVerifyRequest(iFingerprintRequest)
            is IFingerprintIdentifyRequest ->
                fromFingerprintToDomainIdentifyRequest(iFingerprintRequest)
            else -> throw InvalidRequestForFingerprintException("Could not convert to domain request")
        }

    private fun fromFingerprintToDomainIdentifyRequest(iFingerprintRequest: IFingerprintIdentifyRequest): FingerprintIdentifyRequest =
        with(iFingerprintRequest) {
            FingerprintIdentifyRequest(
                projectId, userId, moduleId, metadata, language,
                fingerStatus.mapKeys { it.key.toDomainClass() },
                logoExists, organizationName, programName, matchGroup.fromModuleApiToDomain(), returnIdCount
            )
        }


    private fun fromFingerprintToDomainVerifyRequest(iFingerprintRequest: IFingerprintVerifyRequest): FingerprintVerifyRequest =
        with(iFingerprintRequest) {
            FingerprintVerifyRequest(
                projectId, userId, moduleId, metadata, language,
                fingerStatus.mapKeys { it.key.toDomainClass() },
                logoExists, programName, organizationName, verifyGuid)
        }

    private fun fromFingerprintToDomainCaptureRequest(iFingerprintRequest: IFingerprintCaptureRequest): FingerprintCaptureRequest =
        with(iFingerprintRequest) {
            FingerprintCaptureRequest(
                language, fingerStatus.mapKeys { it.key.toDomainClass() }, activityTitle
            )
        }
}
