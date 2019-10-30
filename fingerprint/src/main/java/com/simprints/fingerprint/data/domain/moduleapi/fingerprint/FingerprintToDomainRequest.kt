package com.simprints.fingerprint.data.domain.moduleapi.fingerprint

import com.simprints.fingerprint.activities.collect.models.toDomainClass
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintMatchRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForFingerprintException
import com.simprints.moduleapi.fingerprint.requests.IFingerprintCaptureRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintMatchRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest

object FingerprintToDomainRequest {

    fun fromFingerprintToDomainRequest(iFingerprintRequest: IFingerprintRequest): FingerprintRequest =
        when (iFingerprintRequest) {
            is IFingerprintCaptureRequest ->
                fromFingerprintToDomainCaptureRequest(iFingerprintRequest)
            is IFingerprintMatchRequest ->
                fromFingerprintToDomainMatchRequest(iFingerprintRequest)
            else -> throw InvalidRequestForFingerprintException("Could not convert to domain request")
        }

    private fun fromFingerprintToDomainCaptureRequest(iFingerprintRequest: IFingerprintCaptureRequest): FingerprintCaptureRequest =
        with(iFingerprintRequest) {
            FingerprintCaptureRequest(
                fingerprintsToCapture.map { it.toDomainClass() }
            )
        }

    private fun fromFingerprintToDomainMatchRequest(iFingerprintRequest: IFingerprintMatchRequest): FingerprintMatchRequest =
        with(iFingerprintRequest) {
            FingerprintMatchRequest(probeFingerprintSamples.map {
                Fingerprint(
                    it.fingerIdentifier.toDomainClass(),
                    it.template
                )
            }, queryForCandidates)
        }
}
