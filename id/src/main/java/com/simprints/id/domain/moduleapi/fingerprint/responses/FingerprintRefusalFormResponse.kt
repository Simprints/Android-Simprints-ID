package com.simprints.id.domain.moduleapi.fingerprint.responses

import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintRefusalFormReason
import com.simprints.moduleapi.fingerprint.responses.IFingerprintExitFormResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintExitReason
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintRefusalFormResponse(val reason: FingerprintRefusalFormReason,
                                          val optionalText: String = ""): FingerprintResponse {
    @IgnoredOnParcel override val type: FingerprintResponseType = FingerprintResponseType.REFUSAL
}

fun IFingerprintExitFormResponse.fromModuleApiToDomain(): FingerprintRefusalFormResponse {
    val reason = when (this.reason) {
        IFingerprintExitReason.REFUSED_RELIGION -> FingerprintRefusalFormReason.REFUSED_RELIGION
        IFingerprintExitReason.REFUSED_DATA_CONCERNS -> FingerprintRefusalFormReason.REFUSED_DATA_CONCERNS
        IFingerprintExitReason.REFUSED_PERMISSION -> FingerprintRefusalFormReason.REFUSED_PERMISSION
        IFingerprintExitReason.SCANNER_NOT_WORKING -> FingerprintRefusalFormReason.SCANNER_NOT_WORKING
        IFingerprintExitReason.REFUSED_NOT_PRESENT -> FingerprintRefusalFormReason.REFUSED_NOT_PRESENT
        IFingerprintExitReason.REFUSED_YOUNG -> FingerprintRefusalFormReason.REFUSED_YOUNG
        IFingerprintExitReason.OTHER -> FingerprintRefusalFormReason.OTHER
    }

    return FingerprintRefusalFormResponse(reason, extra)
}
