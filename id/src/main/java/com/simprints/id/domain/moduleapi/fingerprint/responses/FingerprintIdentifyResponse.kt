package com.simprints.id.domain.moduleapi.fingerprint.responses

import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintMatchingResult
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintIdentifyResponse(
    val identifications: List<FingerprintMatchingResult>
) : FingerprintResponse {
    @IgnoredOnParcel override val type: FingerprintTypeResponse = FingerprintTypeResponse.IDENTIFY
}
