package com.simprints.fingerprint.data.domain.moduleapi.fingerprint

import com.simprints.fingerprint.activities.collect.models.toDomainClass
import com.simprints.fingerprint.data.domain.matching.toDomainClass
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintIdentifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintVerifyRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintEnrolRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintIdentifyRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintVerifyRequest

fun fromFingerprintToDomainRequest(iFingerprintRequest: IFingerprintRequest): FingerprintRequest =
    when (iFingerprintRequest) {
        is IFingerprintEnrolRequest ->
            with(iFingerprintRequest) {
                FingerprintEnrolRequest(
                    projectId, userId, moduleId, metadata, language,
                    fingerStatus.mapKeys { it.key.toDomainClass() },
                    nudgeMode, qualityThreshold, logoExists, programName, organizationName, vibrateMode)
            }
        is IFingerprintVerifyRequest ->
            with(iFingerprintRequest) {
                FingerprintVerifyRequest(
                    projectId, userId, moduleId, metadata, language,
                    fingerStatus.mapKeys { it.key.toDomainClass() },
                    nudgeMode, qualityThreshold, logoExists, programName, organizationName, vibrateMode, verifyGuid)
            }
        is IFingerprintIdentifyRequest ->
            with(iFingerprintRequest) {
                FingerprintIdentifyRequest(
                    projectId, userId, moduleId, metadata, language,
                    fingerStatus.mapKeys { it.key.toDomainClass() },
                    nudgeMode, qualityThreshold, logoExists, organizationName, programName, vibrateMode, matchGroup.toDomainClass(), returnIdCount
                )
            }
        else -> throw IllegalArgumentException("Invalid Fingerprint AppRequest") //StopShip
    }
