package com.simprints.fingerprint.data.domain.moduleapi.fingerprint

import android.os.Parcelable
import com.simprints.fingerprint.data.domain.fingerprint.fromDomainToModuleApi
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.*
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintErrorReason.*
import com.simprints.fingerprint.data.domain.refusal.RefusalFormReason
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintRefusalFormReason
import com.simprints.moduleapi.common.ISecuredImageRef
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.IFingerprintSample
import com.simprints.moduleapi.fingerprint.responses.*
import com.simprints.moduleapi.fingerprint.responses.entities.IFingerprintCaptureResult
import com.simprints.moduleapi.fingerprint.responses.entities.IFingerprintMatchResult
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

object DomainToFingerprintResponse {

    fun fromDomainToFingerprintErrorResponse(error: FingerprintErrorResponse): IFingerprintErrorResponse =
        IFingerprintErrorResponseImpl(fromFingerprintErrorReasonToErrorResponse(error.reason))

    fun fromDomainToFingerprintCaptureResponse(capture: FingerprintCaptureResponse): IFingerprintCaptureResponse =
        IFingerprintCaptureResponseImpl(capture.fingerprints.map {
            IFingerprintCaptureResultImpl(
                it.fingerId.fromDomainToModuleApi(),
                IFingerprintSampleImpl(
                    it.fingerId.fromDomainToModuleApi(),
                    it.templateBytes,
                    it.qualityScore,
                    null
                ))
        })

    fun fromDomainToFingerprintMatchResponse(matchResponse: FingerprintMatchResponse): IFingerprintMatchResponse =
        IFingerprintMatchResponseImpl(matchResponse.result.map {
            IFingerprintMatchResultImpl(
                personId = it.guid,
                confidenceScore = it.confidence
            )
        })

    fun fromDomainToFingerprintRefusalFormResponse(refusalResponse: FingerprintRefusalFormResponse): IFingerprintExitFormResponse {

        val reason = when(refusalResponse.reason) {
            RefusalFormReason.REFUSED_RELIGION -> IFingerprintExitReason.REFUSED_RELIGION
            RefusalFormReason.REFUSED_DATA_CONCERNS -> IFingerprintExitReason.REFUSED_DATA_CONCERNS
            RefusalFormReason.REFUSED_PERMISSION -> IFingerprintExitReason.REFUSED_PERMISSION
            RefusalFormReason.SCANNER_NOT_WORKING -> IFingerprintExitReason.SCANNER_NOT_WORKING
            RefusalFormReason.REFUSED_NOT_PRESENT -> IFingerprintExitReason.REFUSED_NOT_PRESENT
            RefusalFormReason.REFUSED_YOUNG -> IFingerprintExitReason.REFUSED_YOUNG
            RefusalFormReason.OTHER -> IFingerprintExitReason.OTHER
        }

        return IFingerprintExitFormResponseImpl(reason, refusalResponse.extra)
    }

    private fun fromFingerprintErrorReasonToErrorResponse(reason: FingerprintErrorReason) =
        when (reason) {
            UNEXPECTED_ERROR -> IFingerprintErrorReason.UNEXPECTED_ERROR
            BLUETOOTH_NOT_SUPPORTED -> IFingerprintErrorReason.BLUETOOTH_NOT_SUPPORTED
        }

}

@Parcelize
private class IFingerprintErrorResponseImpl(override val error: IFingerprintErrorReason) : IFingerprintErrorResponse {
    @IgnoredOnParcel override val type: IFingerprintResponseType = IFingerprintResponseType.ERROR
}

@Parcelize
private class IFingerprintCaptureResponseImpl(override val captureResult: List<IFingerprintCaptureResult>) : IFingerprintCaptureResponse {
    @IgnoredOnParcel override val type: IFingerprintResponseType = IFingerprintResponseType.CAPTURE
}

@Parcelize
private class IFingerprintExitFormResponseImpl(
    override val reason: IFingerprintExitReason,
    override val extra: String) : IFingerprintExitFormResponse {
    @IgnoredOnParcel override val type: IFingerprintResponseType = IFingerprintResponseType.REFUSAL
}

@Parcelize
private data class IFingerprintMatchResponseImpl(
    override val result: List<IFingerprintMatchResult>
) : Parcelable, IFingerprintMatchResponse {
    @IgnoredOnParcel override val type: IFingerprintResponseType = IFingerprintResponseType.MATCH
}

@Parcelize
private data class IFingerprintMatchResultImpl(
    override val personId: String,
    override val confidenceScore: Float) : Parcelable, IFingerprintMatchResult

@Parcelize
private class IFingerprintCaptureResultImpl(
    override val identifier: IFingerIdentifier,
    override val sample: IFingerprintSample?) : IFingerprintCaptureResult

@Parcelize
private class IFingerprintSampleImpl(
    override val fingerIdentifier: IFingerIdentifier,
    override val template: ByteArray,
    override val templateQualityScore: Int,
    override val imageRef: ISecuredImageRef?) : IFingerprintSample
