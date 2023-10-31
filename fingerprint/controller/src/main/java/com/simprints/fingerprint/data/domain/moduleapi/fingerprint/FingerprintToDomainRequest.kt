package com.simprints.fingerprint.data.domain.moduleapi.fingerprint

import com.simprints.fingerprint.data.domain.fingerprint.fromModuleApiToDomain
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForFingerprintException
import com.simprints.moduleapi.fingerprint.requests.IFingerprintCaptureRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest

/**
 * This is a singleton class that helps convert ModuleApi's module request objects to Fingerprint's
 * module equivalent.
 */
object FingerprintToDomainRequest {

    fun fromFingerprintToDomainRequest(iFingerprintRequest: IFingerprintRequest): FingerprintRequest =
        when (iFingerprintRequest) {
            is IFingerprintCaptureRequest ->
                fromFingerprintToDomainCaptureRequest(iFingerprintRequest)
            else -> throw InvalidRequestForFingerprintException("Could not convert to domain request")
        }

    private fun fromFingerprintToDomainCaptureRequest(iFingerprintRequest: IFingerprintCaptureRequest): FingerprintCaptureRequest =
        with(iFingerprintRequest) {
            FingerprintCaptureRequest(
                fingerprintsToCapture.map { it.fromModuleApiToDomain() }
            )
        }
}
