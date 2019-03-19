package com.simprints.id.moduleapi

import com.simprints.id.domain.matching.IdentificationResult
import com.simprints.id.domain.matching.Tier
import com.simprints.id.domain.responses.EnrolResponse
import com.simprints.id.domain.responses.IdentifyResponse
import com.simprints.id.domain.responses.VerifyResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintEnrolResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintIdentifyResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponseTier
import com.simprints.moduleapi.fingerprint.responses.IFingerprintVerifyResponse

object FingerprintToDomainAdapter {

    fun fromFingerprintToDomainVerifyResponse(fingerprintResponse: IFingerprintVerifyResponse): VerifyResponse =
        VerifyResponse(fingerprintResponse.guid, fingerprintResponse.confidence, fromFingerprintToDomainTier(fingerprintResponse.tier))

    fun fromFingerprintToDomainEnrolResponse(fingerprintResponse: IFingerprintEnrolResponse): EnrolResponse =
            EnrolResponse(fingerprintResponse.guid)

    fun fromFingerprintToDomainIdentifyResponse(fingerprintResponse: IFingerprintIdentifyResponse, currentSessionId: String): IdentifyResponse =
        IdentifyResponse(fingerprintResponse.identifications.map { fromFingerprintToDomainIdentificationResult(it) }, currentSessionId)

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
