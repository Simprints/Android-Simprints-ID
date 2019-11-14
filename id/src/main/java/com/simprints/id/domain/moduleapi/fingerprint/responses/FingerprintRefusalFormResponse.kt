package com.simprints.id.domain.moduleapi.fingerprint.responses

import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintRefusalFormReason
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.fromModuleApiToDomain
import com.simprints.moduleapi.fingerprint.responses.IFingerprintExitFormResponse
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintRefusalFormResponse(val reason: FingerprintRefusalFormReason,
                                          val optionalText: String = ""): FingerprintResponse {
    @IgnoredOnParcel override val type: FingerprintResponseType = FingerprintResponseType.REFUSAL
}

fun IFingerprintExitFormResponse.fromModuleApiToDomain() = FingerprintRefusalFormResponse(reason.fromModuleApiToDomain(), extra)
