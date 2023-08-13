package com.simprints.fingerprint.data.domain.moduleapi.fingerprint

import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import com.simprints.fingerprint.data.domain.fingerprint.fromModuleApiToDomain
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintConfigurationRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintMatchRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForFingerprintException
import com.simprints.moduleapi.fingerprint.requests.IFingerprintCaptureRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintConfigurationRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintMatchRequest
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
            is IFingerprintMatchRequest ->
                fromFingerprintToDomainMatchRequest(iFingerprintRequest)
            is IFingerprintConfigurationRequest ->
                fromFingerprintToDomainConfigurationRequest(iFingerprintRequest)
            else -> throw InvalidRequestForFingerprintException("Could not convert to domain request")
        }

    private fun fromFingerprintToDomainCaptureRequest(iFingerprintRequest: IFingerprintCaptureRequest): FingerprintCaptureRequest =
        with(iFingerprintRequest) {
            FingerprintCaptureRequest(
                fingerprintsToCapture.map { it.fromModuleApiToDomain() }
            )
        }

    private fun fromFingerprintToDomainMatchRequest(iFingerprintRequest: IFingerprintMatchRequest): FingerprintMatchRequest =
        with(iFingerprintRequest) {
            FingerprintMatchRequest(probeFingerprintSamples.map {
                Fingerprint(
                    it.fingerIdentifier.fromModuleApiToDomain(), it.template, it.format
                )
            }, queryForCandidates)
        }

    @Suppress("UNUSED_PARAMETER")
    private fun fromFingerprintToDomainConfigurationRequest(iFingerprintRequest: IFingerprintConfigurationRequest): FingerprintConfigurationRequest =
        FingerprintConfigurationRequest()
}
