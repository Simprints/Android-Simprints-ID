package com.simprints.id.moduleapi

import com.simprints.id.domain.matching.IdentificationResult
import com.simprints.id.domain.matching.Tier
import com.simprints.id.domain.responses.EnrolResponse
import com.simprints.id.domain.responses.IdentifyResponse
import com.simprints.id.domain.responses.Response
import com.simprints.id.domain.responses.VerifyResponse
import com.simprints.moduleapi.fingerprint.responses.*

object FingerprintToDomainAdapter {

    fun fromFingerprintToDomainResponse(fingerprintResponse: IFingerprintResponse): Response =
        when (fingerprintResponse) {
            is IFingerprintEnrolResponse -> fromFingerprintToDomainEnrolResponse(fingerprintResponse)
            is IFingerprintVerifyResponse -> fromFingerprintToDomainVerifyResponse(fingerprintResponse)
            is IFingerprintIdentifyResponse -> fromFingerprintToDomainIdentifyResponse(fingerprintResponse)
            else -> throw IllegalStateException("Invalid fingerprint request")
        }

    private fun fromFingerprintToDomainVerifyResponse(fingerprintResponse: IFingerprintVerifyResponse): VerifyResponse =
        VerifyResponse(fingerprintResponse.guid, fingerprintResponse.confidence, fromFingerprintToDomainTier(fingerprintResponse.tier))

    private fun fromFingerprintToDomainEnrolResponse(fingerprintResponse: IFingerprintEnrolResponse): EnrolResponse =
            EnrolResponse(fingerprintResponse.guid)

    private fun fromFingerprintToDomainIdentifyResponse(fingerprintResponse: IFingerprintIdentifyResponse): IdentifyResponse =
        IdentifyResponse(fingerprintResponse.identifications.map { fromFingerprintToDomainIdentificationResult(it) }, fingerprintResponse.sessionId)

    private fun fromFingerprintToDomainIdentificationResult(identification: IFingerprintIdentifyResponse.IIdentificationResult): IdentificationResult =
        IdentificationResult(identification.guid, identification.confidence, fromFingerprintToDomainTier(identification.tier))

    private fun fromFingerprintToDomainTier(tier: IFingerprintResponseTier): Tier =
        when (tier) {
            IFingerprintResponseTier.TIER_1 -> Tier.TIER_1
            IFingerprintResponseTier.TIER_2 -> Tier.TIER_2
            IFingerprintResponseTier.TIER_3 -> Tier.TIER_3
            IFingerprintResponseTier.TIER_4 -> Tier.TIER_4
            IFingerprintResponseTier.TIER_5 -> Tier.TIER_5
        }
}
