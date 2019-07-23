package com.simprints.fingerprint.data.domain.moduleapi.fingerprint

import android.os.Parcelable
import com.simprints.fingerprint.data.domain.matching.MatchingResult
import com.simprints.fingerprint.data.domain.matching.MatchingTier
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.*
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintErrorReason.*
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintRefusalFormReason
import com.simprints.moduleapi.fingerprint.responses.*
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

object DomainToFingerprintResponse {

    fun fromDomainToFingerprintErrorResponse(error: FingerprintErrorResponse): IFingerprintErrorResponse =
        IFingerprintErrorResponseImpl(fromFingerprintErrorReasonToErrorResponse(error.reason))

    fun fromDomainToFingerprintEnrolResponse(enrol: FingerprintEnrolResponse): IFingerprintEnrolResponse = IFingerprintEnrolResponseImpl(enrol.guid)

    fun fromDomainToFingerprintVerifyResponse(verify: FingerprintVerifyResponse): IFingerprintVerifyResponse {
        val matchingResult = IMatchingResultImpl(verify.guid, verify.confidence, toIFingerprintResponseTier(verify.tier))
        return IFingerprintVerifyResponseImpl(matchingResult)
    }

    fun fromDomainToFingerprintIdentifyResponse(identify: FingerprintIdentifyResponse): IFingerprintIdentifyResponse =
        IFingerprintIdentifyResponseImpl(identify.identifications.map { fromDomainToFingerprintIdentificationResult(it) })

    fun fromDomainToFingerprintRefusalFormResponse(refusalResponse: FingerprintRefusalFormResponse): IFingerprintRefusalFormResponse {

        val reason = when(refusalResponse.reason) {
            FingerprintRefusalFormReason.REFUSED_RELIGION -> IFingerprintRefusalReason.REFUSED_RELIGION
            FingerprintRefusalFormReason.REFUSED_DATA_CONCERNS -> IFingerprintRefusalReason.REFUSED_DATA_CONCERNS
            FingerprintRefusalFormReason.REFUSED_PERMISSION -> IFingerprintRefusalReason.REFUSED_PERMISSION
            FingerprintRefusalFormReason.SCANNER_NOT_WORKING -> IFingerprintRefusalReason.SCANNER_NOT_WORKING
            FingerprintRefusalFormReason.REFUSED_NOT_PRESENT -> IFingerprintRefusalReason.REFUSED_NOT_PRESENT
            FingerprintRefusalFormReason.REFUSED_YOUNG -> IFingerprintRefusalReason.REFUSED_YOUNG
            FingerprintRefusalFormReason.OTHER -> IFingerprintRefusalReason.OTHER
        }

        return IFingerprintRefusalFormResponseImpl(reason, refusalResponse.extra)
    }


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

    private fun fromFingerprintErrorReasonToErrorResponse(reason: FingerprintErrorReason) =
        when (reason) {
            UNEXPECTED_ERROR -> IFingerprintErrorReason.UNEXPECTED_ERROR
            BLUETOOTH_NOT_SUPPORTED -> IFingerprintErrorReason.BLUETOOTH_NOT_SUPPORTED
            GUID_NOT_FOUND_ONLINE -> IFingerprintErrorReason.GUID_NOT_FOUND_ONLINE
        }

}

@Parcelize
private class IFingerprintErrorResponseImpl(override val error: IFingerprintErrorReason) : IFingerprintErrorResponse {
    @IgnoredOnParcel override val type: IFingerprintResponseType = IFingerprintResponseType.ERROR
}

@Parcelize
private class IFingerprintEnrolResponseImpl(override val guid: String) : IFingerprintEnrolResponse {
    @IgnoredOnParcel override val type: IFingerprintResponseType = IFingerprintResponseType.ENROL
}

@Parcelize
private class IFingerprintIdentifyResponseImpl(
    override val identifications: List<IMatchingResult>) : IFingerprintIdentifyResponse {
    @IgnoredOnParcel override val type: IFingerprintResponseType = IFingerprintResponseType.IDENTIFY
}

@Parcelize
private class IFingerprintRefusalFormResponseImpl(
    override val reason: IFingerprintRefusalReason,
    override val extra: String) : IFingerprintRefusalFormResponse {
    @IgnoredOnParcel override val type: IFingerprintResponseType = IFingerprintResponseType.REFUSAL
}

@Parcelize
private class IFingerprintVerifyResponseImpl(override val matchingResult: IMatchingResult) : IFingerprintVerifyResponse {
    @IgnoredOnParcel override val type: IFingerprintResponseType = IFingerprintResponseType.VERIFY
}

@Parcelize
private data class IMatchingResultImpl(
    override val guid: String,
    override val confidence: Int,
    override val tier: IFingerprintResponseTier) : Parcelable, IMatchingResult
