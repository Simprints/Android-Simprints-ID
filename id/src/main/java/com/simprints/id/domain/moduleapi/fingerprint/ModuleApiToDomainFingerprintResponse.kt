package com.simprints.id.domain.moduleapi.fingerprint

import com.simprints.id.domain.moduleapi.fingerprint.responses.*
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintRefusalFormReason
import com.simprints.moduleapi.fingerprint.responses.*

object ModuleApiToDomainFingerprintResponse {

    fun fromModuleApiToDomainFingerprintResponse(fingerprintResponse: IFingerprintResponse): FingerprintResponse =
        when (fingerprintResponse.type) {
            IFingerprintResponseType.ENROL -> fromModuleApiToFingerprintEnrolResponse(fingerprintResponse as IFingerprintEnrolResponse)
            IFingerprintResponseType.MATCH -> TODO("PAS-391")
            IFingerprintResponseType.REFUSAL -> fromModuleApiToFingerprintRefusalResponse(fingerprintResponse as IFingerprintExitFormResponse)
            IFingerprintResponseType.ERROR -> fromModuleApiToFingerprintErrorResponse(fingerprintResponse as IFingerprintErrorResponse)
        }

    private fun fromModuleApiToFingerprintErrorResponse(fingerprintResponse: IFingerprintErrorResponse): FingerprintErrorResponse =
        FingerprintErrorResponse(fromFingerprintToDomainError(fingerprintResponse.error))


    private fun fromModuleApiToFingerprintEnrolResponse(fingerprintResponse: IFingerprintEnrolResponse): FingerprintEnrolResponse =
        FingerprintEnrolResponse(fingerprintResponse.guid)

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
}
