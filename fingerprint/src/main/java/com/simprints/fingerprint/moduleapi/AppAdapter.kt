package com.simprints.fingerprint.moduleapi

import android.os.Parcelable
import com.simprints.fingerprint.data.domain.matching.result.MatchingResult
import com.simprints.fingerprint.data.domain.matching.result.MatchingTier
import com.simprints.fingerprint.data.domain.requests.FingerprintEnrolRequest
import com.simprints.fingerprint.data.domain.requests.FingerprintIdentifyRequest
import com.simprints.fingerprint.data.domain.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.requests.FingerprintVerifyRequest
import com.simprints.fingerprint.data.domain.responses.FingerprintEnrolResponse
import com.simprints.fingerprint.data.domain.responses.FingerprintIdentifyResponse
import com.simprints.fingerprint.data.domain.responses.FingerprintRefusalFormResponse
import com.simprints.fingerprint.data.domain.responses.FingerprintVerifyResponse
import com.simprints.moduleapi.fingerprint.requests.IFingerprintEnrolRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintIdentifyRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import com.simprints.moduleapi.fingerprint.requests.IFingerprintVerifyRequest
import com.simprints.moduleapi.fingerprint.responses.*
import kotlinx.android.parcel.Parcelize
import java.security.InvalidParameterException

object AppAdapter {

    fun fromModuleApiToDomainRequest(iFingerprintEnrolRequest: IFingerprintRequest): FingerprintRequest =
        when (iFingerprintEnrolRequest) {
            is IFingerprintEnrolRequest -> FingerprintEnrolRequest(iFingerprintEnrolRequest)
            is IFingerprintVerifyRequest -> FingerprintVerifyRequest(iFingerprintEnrolRequest)
            is IFingerprintIdentifyRequest -> FingerprintIdentifyRequest(iFingerprintEnrolRequest)
            else -> throw InvalidParameterException("Invalid Fingerprint Request") //StopShip
        }

    fun fromDomainToModuleApiEnrolResponse(enrol: FingerprintEnrolResponse): IFingerprintEnrolResponse = IFingerprintEnrolResponseImpl(enrol.guid)

    fun fromDomainToModuleApiVerifyResponse(verify: FingerprintVerifyResponse): IFingerprintVerifyResponse =
        IFingerprintVerifyResponseImpl(verify.guid, verify.confidence, toIFingerprintResponseTier(verify.tier))

    fun fromDomainToModuleApiIdentifyResponse(identify: FingerprintIdentifyResponse): IFingerprintIdentifyResponse =
        IFingerprintIdentifyResponseImpl(identify.identifications.map { fromDomainToModuleApiIdentificationResult(it) })

    fun fromDomainToModuleApiRefusalFormResponse(refusaResponse: FingerprintRefusalFormResponse): IFingerprintRefusalFormResponse =
        IFingerprintRefusalFormResponseImpl(refusaResponse.reason, refusaResponse.extra)

    private fun fromDomainToModuleApiIdentificationResult(result: MatchingResult): IFingerprintIdentifyResponse.IIdentificationResult =
        IIdentificationResultImpl(result.guid, result.confidence, toIFingerprintResponseTier(result.tier))

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
    override val identifications: List<IFingerprintIdentifyResponse.IIdentificationResult>) : IFingerprintIdentifyResponse

@Parcelize
private class IFingerprintRefusalFormResponseImpl(
    override val reason: String,
    override val extra: String) : IFingerprintRefusalFormResponse

@Parcelize
private class IFingerprintVerifyResponseImpl(override val guid: String,
                                             override val confidence: Int,
                                             override val tier: IFingerprintResponseTier) : IFingerprintVerifyResponse

@Parcelize
private data class IIdentificationResultImpl(
    override val guid: String,
    override val confidence: Int,
    override val tier: IFingerprintResponseTier) : Parcelable, IFingerprintIdentifyResponse.IIdentificationResult

