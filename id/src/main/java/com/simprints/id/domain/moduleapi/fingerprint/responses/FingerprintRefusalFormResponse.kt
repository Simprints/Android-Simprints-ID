package com.simprints.id.domain.moduleapi.fingerprint.responses

import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintRefusalFormReason
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintRefusalFormResponse(val reason: FingerprintRefusalFormReason,
                                          val optionalText: String = ""): FingerprintResponse {
    @IgnoredOnParcel override val type: FingerprintTypeResponse = FingerprintTypeResponse.REFUSAL
}
