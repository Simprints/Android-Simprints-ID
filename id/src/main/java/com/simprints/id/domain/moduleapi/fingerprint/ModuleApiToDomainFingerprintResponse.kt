package com.simprints.id.domain.moduleapi.fingerprint

import com.simprints.id.domain.moduleapi.fingerprint.responses.*
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintMatchingResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintRefusalFormReason
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintTier
import com.simprints.moduleapi.fingerprint.responses.*

object ModuleApiToDomainFingerprintResponse {

    fun fromModuleApiToDomainFingerprintResponse(fingerprintResponse: IFingerprintResponse): FingerprintResponse =
        when (fingerprintResponse.type) {
            IFingerprintResponseType.CAPTURE -> fromModuleApiToFingerprintCaptureResponse(fingerprintResponse as IFingerprintCaptureResponse)
            IFingerprintResponseType.VERIFY -> fromModuleApiToFingerprintVerifyResponse(fingerprintResponse as IFingerprintVerifyResponse)
            IFingerprintResponseType.IDENTIFY -> fromModuleApiToFingerprintIdentifyResponse(fingerprintResponse as IFingerprintIdentifyResponse)
            IFingerprintResponseType.REFUSAL -> fromModuleApiToFingerprintRefusalResponse(fingerprintResponse as IFingerprintExitFormResponse)
            IFingerprintResponseType.ERROR -> fromModuleApiToFingerprintErrorResponse(fingerprintResponse as IFingerprintErrorResponse)
        }

    private fun fromModuleApiToFingerprintCaptureResponse(fingerprintResponse: IFingerprintCaptureResponse): FingerprintCaptureResponse {
        return FingerprintCaptureResponse(captureResult = fingerprintResponse.captureResult)
    }

    private fun fromModuleApiToFingerprintVerifyResponse(fingerprintResponse: IFingerprintVerifyResponse): FingerprintVerifyResponse {
        val matchResult = FingerprintMatchingResult(
            fingerprintResponse.matchingResult.guid,
            fingerprintResponse.matchingResult.confidence,
            fromFingerprintToDomainTier(fingerprintResponse.matchingResult.tier))

        return FingerprintVerifyResponse(matchResult)
    }

    private fun fromModuleApiToFingerprintErrorResponse(fingerprintResponse: IFingerprintErrorResponse): FingerprintErrorResponse =
        FingerprintErrorResponse(fromFingerprintToDomainError(fingerprintResponse.error))

    private fun fromModuleApiToFingerprintIdentifyResponse(fingerprintResponse: IFingerprintIdentifyResponse): FingerprintIdentifyResponse =
        FingerprintIdentifyResponse(fingerprintResponse.identifications.map { fromFingerprintToDomainMatchingResult(it) })

    private fun fromFingerprintToDomainMatchingResult(matchingResult: IMatchingResult): FingerprintMatchingResult =
        FingerprintMatchingResult(matchingResult.guid, matchingResult.confidence, fromFingerprintToDomainTier(matchingResult.tier))

    private fun fromModuleApiToFingerprintRefusalResponse(fingerprintResponse: IFingerprintExitFormResponse): FingerprintResponse {
        val reason = when(fingerprintResponse.reason) {
            IFingerprintExitReason.REFUSED_RELIGION -> FingerprintRefusalFormReason.REFUSED_RELIGION
            IFingerprintExitReason.REFUSED_DATA_CONCERNS -> FingerprintRefusalFormReason.REFUSED_DATA_CONCERNS
            IFingerprintExitReason.REFUSED_PERMISSION -> FingerprintRefusalFormReason.REFUSED_PERMISSION
            IFingerprintExitReason.SCANNER_NOT_WORKING -> FingerprintRefusalFormReason.SCANNER_NOT_WORKING
            IFingerprintExitReason.REFUSED_NOT_PRESENT -> FingerprintRefusalFormReason.REFUSED_NOT_PRESENT
            IFingerprintExitReason.REFUSED_YOUNG -> FingerprintRefusalFormReason.REFUSED_YOUNG
            IFingerprintExitReason.OTHER -> FingerprintRefusalFormReason.OTHER
        }

        return FingerprintRefusalFormResponse(reason, fingerprintResponse.extra)
    }

    private fun fromFingerprintToDomainError(error: IFingerprintErrorReason): FingerprintErrorReason =
        when(error) {
            IFingerprintErrorReason.UNEXPECTED_ERROR -> FingerprintErrorReason.UNEXPECTED_ERROR
            IFingerprintErrorReason.BLUETOOTH_NOT_SUPPORTED -> FingerprintErrorReason.BLUETOOTH_NOT_SUPPORTED
            IFingerprintErrorReason.GUID_NOT_FOUND_ONLINE -> FingerprintErrorReason.GUID_NOT_FOUND_ONLINE
        }

    private fun fromFingerprintToDomainTier(tier: IFingerprintResponseTier): FingerprintTier =
        when (tier) {
            IFingerprintResponseTier.TIER_1 -> FingerprintTier.TIER_1
            IFingerprintResponseTier.TIER_2 -> FingerprintTier.TIER_2
            IFingerprintResponseTier.TIER_3 -> FingerprintTier.TIER_3
            IFingerprintResponseTier.TIER_4 -> FingerprintTier.TIER_4
            IFingerprintResponseTier.TIER_5 -> FingerprintTier.TIER_5
        }

}
