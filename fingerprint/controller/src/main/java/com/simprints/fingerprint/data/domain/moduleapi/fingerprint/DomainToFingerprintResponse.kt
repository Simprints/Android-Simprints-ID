package com.simprints.fingerprint.data.domain.moduleapi.fingerprint

import com.simprints.fingerprint.data.domain.fingerprint.fromDomainToModuleApi
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintErrorReason
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintErrorReason.BLUETOOTH_NOT_SUPPORTED
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintErrorReason.BLUETOOTH_NO_PERMISSION
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintErrorReason.UNEXPECTED_ERROR
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintErrorResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintRefusalFormResponse
import com.simprints.fingerprint.data.domain.refusal.RefusalFormReason
import com.simprints.moduleapi.common.IPath
import com.simprints.moduleapi.common.ISecuredImageRef
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.IFingerprintSample
import com.simprints.moduleapi.fingerprint.responses.IFingerprintCaptureResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintErrorReason
import com.simprints.moduleapi.fingerprint.responses.IFingerprintErrorResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintExitFormResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintExitReason
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponseType
import com.simprints.moduleapi.fingerprint.responses.entities.IFingerprintCaptureResult
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * This class is a singleton helper that helps convert Fingerprint's module response objects into
 * ModuleApi's equivalent.
 */
object DomainToFingerprintResponse {

    fun fromDomainToFingerprintErrorResponse(error: FingerprintErrorResponse): IFingerprintErrorResponse =
        IFingerprintErrorResponseImpl(fromFingerprintErrorReasonToErrorResponse(error.reason))

    fun fromDomainToFingerprintCaptureResponse(capture: FingerprintCaptureResponse): IFingerprintCaptureResponse =
        IFingerprintCaptureResponseImpl(capture.fingerprints.map { fingerprint ->
            IFingerprintCaptureResultImpl(
                fingerprint.fingerId.fromDomainToModuleApi(),
                IFingerprintSampleImpl(
                    fingerprint.fingerId.fromDomainToModuleApi(),
                    fingerprint.templateBytes,
                    fingerprint.qualityScore,
                    fingerprint.imageRef?.path?.let {
                        ISecuredImageRefImpl(IPathImpl(it.parts))
                    },
                    fingerprint.format
                )
            )
        })

    fun fromDomainToFingerprintRefusalFormResponse(refusalResponse: FingerprintRefusalFormResponse): IFingerprintExitFormResponse {

        val reason = when (refusalResponse.reason) {
            RefusalFormReason.REFUSED_RELIGION -> IFingerprintExitReason.REFUSED_RELIGION
            RefusalFormReason.REFUSED_DATA_CONCERNS -> IFingerprintExitReason.REFUSED_DATA_CONCERNS
            RefusalFormReason.REFUSED_PERMISSION -> IFingerprintExitReason.REFUSED_PERMISSION
            RefusalFormReason.SCANNER_NOT_WORKING -> IFingerprintExitReason.SCANNER_NOT_WORKING
            RefusalFormReason.REFUSED_NOT_PRESENT -> IFingerprintExitReason.REFUSED_NOT_PRESENT
            RefusalFormReason.REFUSED_YOUNG -> IFingerprintExitReason.REFUSED_YOUNG
            RefusalFormReason.APP_NOT_WORKING -> IFingerprintExitReason.APP_NOT_WORKING
            RefusalFormReason.OTHER -> IFingerprintExitReason.OTHER
        }

        return IFingerprintExitFormResponseImpl(reason, refusalResponse.extra)
    }

    private fun fromFingerprintErrorReasonToErrorResponse(reason: FingerprintErrorReason) =
        when (reason) {
            UNEXPECTED_ERROR -> IFingerprintErrorReason.UNEXPECTED_ERROR
            BLUETOOTH_NOT_SUPPORTED -> IFingerprintErrorReason.BLUETOOTH_NOT_SUPPORTED
            BLUETOOTH_NO_PERMISSION -> IFingerprintErrorReason.BLUETOOTH_NO_PERMISSION
        }
}

// The following classes represent the corresponding implementation of ModuleApi response interfaces.

@Parcelize
private class IFingerprintErrorResponseImpl(override val error: IFingerprintErrorReason) : IFingerprintErrorResponse {
    @IgnoredOnParcel
    override val type: IFingerprintResponseType = IFingerprintResponseType.ERROR
}

@Parcelize
private class IFingerprintCaptureResponseImpl(override val captureResult: List<IFingerprintCaptureResult>) :
    IFingerprintCaptureResponse {
    @IgnoredOnParcel
    override val type: IFingerprintResponseType = IFingerprintResponseType.CAPTURE
}

@Parcelize
private class IFingerprintExitFormResponseImpl(
    override val reason: IFingerprintExitReason,
    override val extra: String
) : IFingerprintExitFormResponse {
    @IgnoredOnParcel
    override val type: IFingerprintResponseType = IFingerprintResponseType.REFUSAL
}

@Parcelize
private class IFingerprintCaptureResultImpl(
    override val identifier: IFingerIdentifier,
    override val sample: IFingerprintSample?
) : IFingerprintCaptureResult

@Parcelize
private class IFingerprintSampleImpl(
    override val fingerIdentifier: IFingerIdentifier,
    override val template: ByteArray,
    override val templateQualityScore: Int,
    override val imageRef: ISecuredImageRef?,
    override val format: String
) : IFingerprintSample

@Parcelize
private class ISecuredImageRefImpl(
    override val path: IPath
) : ISecuredImageRef

@Parcelize
private class IPathImpl(
    override val parts: Array<String>
) : IPath
