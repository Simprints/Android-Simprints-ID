package com.simprints.id.domain.moduleapi.fingerprint

import com.simprints.id.domain.moduleapi.fingerprint.responses.*
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintMatchingResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintRefusalFormReason
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintTier
import com.simprints.moduleapi.fingerprint.responses.*

object FingerprintToDomainResponse {

    fun fromFingerprintToDomainResponse(fingerprintResponse: IFingerprintResponse): FingerprintResponse =
        when (fingerprintResponse) {
            is IFingerprintEnrolResponse -> fromFingerprintToDomainEnrolResponse(fingerprintResponse)
            is IFingerprintVerifyResponse -> fromFingerprintToDomainVerifyResponse(fingerprintResponse)
            is IFingerprintIdentifyResponse -> fromFingerprintToDomainIdentifyResponse(fingerprintResponse)
            is IFingerprintRefusalFormResponse -> fromFingerprintToDomainRefusalResponse(fingerprintResponse)
            else -> throw IllegalArgumentException("Invalid fingerprint request")
        }

    private fun fromFingerprintToDomainVerifyResponse(fingerprintResponse: IFingerprintVerifyResponse): FingerprintVerifyResponse {
        val matchResult = FingerprintMatchingResult(
            fingerprintResponse.matchingResult.guid,
            fingerprintResponse.matchingResult.confidence,
            fromFingerprintToDomainTier(fingerprintResponse.matchingResult.tier))

        return FingerprintVerifyResponse(matchResult)
    }

    private fun fromFingerprintToDomainEnrolResponse(fingerprintResponse: IFingerprintEnrolResponse): FingerprintEnrolResponse =
        FingerprintEnrolResponse(fingerprintResponse.guid)

    private fun fromFingerprintToDomainIdentifyResponse(fingerprintResponse: IFingerprintIdentifyResponse): FingerprintIdentifyResponse =
        FingerprintIdentifyResponse(fingerprintResponse.identifications.map { fromFingerprintToDomainMatchingResult(it) })

    private fun fromFingerprintToDomainMatchingResult(matchingResult: IMatchingResult): FingerprintMatchingResult =
        FingerprintMatchingResult(matchingResult.guid, matchingResult.confidence, fromFingerprintToDomainTier(matchingResult.tier))

    private fun fromFingerprintToDomainRefusalResponse(fingerprintResponse: IFingerprintRefusalFormResponse): FingerprintResponse =
        FingerprintRefusalFormResponse(FingerprintRefusalFormReason.valueOf(fingerprintResponse.reason), fingerprintResponse.extra)


    private fun fromFingerprintToDomainTier(tier: IFingerprintResponseTier): FingerprintTier =
        when (tier) {
            IFingerprintResponseTier.TIER_1 -> FingerprintTier.TIER_1
            IFingerprintResponseTier.TIER_2 -> FingerprintTier.TIER_2
            IFingerprintResponseTier.TIER_3 -> FingerprintTier.TIER_3
            IFingerprintResponseTier.TIER_4 -> FingerprintTier.TIER_4
            IFingerprintResponseTier.TIER_5 -> FingerprintTier.TIER_5
        }
}
