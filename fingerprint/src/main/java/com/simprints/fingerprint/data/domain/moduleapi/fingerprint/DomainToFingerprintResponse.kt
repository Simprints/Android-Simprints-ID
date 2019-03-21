package com.simprints.fingerprint.data.domain.moduleapi.fingerprint

import android.os.Parcelable
import com.simprints.fingerprint.data.domain.matching.result.MatchingResult
import com.simprints.fingerprint.data.domain.matching.result.MatchingTier
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintIdentifyResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintRefusalFormResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintVerifyResponse
import com.simprints.moduleapi.fingerprint.responses.*
import kotlinx.android.parcel.Parcelize

object DomainToFingerprintResponse {

    fun fromDomainToFingerprintEnrolResponse(enrol: FingerprintEnrolResponse): IFingerprintEnrolResponse = IFingerprintEnrolResponseImpl(enrol.guid)

    fun fromDomainToFingerprintVerifyResponse(verify: FingerprintVerifyResponse): IFingerprintVerifyResponse {
        val matchingResult = IMatchingResultImpl(verify.guid, verify.confidence, toIFingerprintResponseTier(verify.tier))
        return IFingerprintVerifyResponseImpl(matchingResult)
    }

    fun fromDomainToFingerprintIdentifyResponse(identify: FingerprintIdentifyResponse): IFingerprintIdentifyResponse =
        IFingerprintIdentifyResponseImpl(identify.identifications.map { fromDomainToFingerprintIdentificationResult(it) })

    fun fromDomainToFingerprintRefusalFormResponse(refusalResponse: FingerprintRefusalFormResponse): IFingerprintRefusalFormResponse =
        IFingerprintRefusalFormResponseImpl(refusalResponse.reason, refusalResponse.extra)

    private fun fromDomainToFingerprintIdentificationResult(result: MatchingResult): IMatchingResult =
        IMatchingResultImpl(result.guid, result.confidence, toIFingerprintResponseTier(result.tier))

    private fun toIFingerprintResponseTier(tier: MatchingTier): IFingerprintResponseTier =
        when (tier) {
            MatchingTier.TIER_1 -> IFingerprintResponseTier.TIER_1
            MatchingTier.TIER_2 -> IFingerprintResponseTier.TIER_2
            MatchingTier.TIER_3 -> IFingerprintResponseTier.TIER_3
            MatchingTier.TIER_4 -> IFingerprintResponseTier.TIER_4
            MatchingTier.TIER_5 -> IFingerprintResponseTier.TIER_5
        }
}

@Parcelize
private class IFingerprintEnrolResponseImpl(override val guid: String) : IFingerprintEnrolResponse

@Parcelize
private class IFingerprintIdentifyResponseImpl(
    override val identifications: List<IMatchingResult>) : IFingerprintIdentifyResponse

@Parcelize
private class IFingerprintRefusalFormResponseImpl(
    override val reason: String,
    override val extra: String) : IFingerprintRefusalFormResponse

@Parcelize
private class IFingerprintVerifyResponseImpl(override val matchingResult: IMatchingResult) : IFingerprintVerifyResponse

@Parcelize
private data class IMatchingResultImpl(
    override val guid: String,
    override val confidence: Int,
    override val tier: IFingerprintResponseTier) : Parcelable, IMatchingResult
