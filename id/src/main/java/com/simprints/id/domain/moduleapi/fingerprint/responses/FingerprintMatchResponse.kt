package com.simprints.id.domain.moduleapi.fingerprint.responses

import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintMatchResult
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintMatchResponse(
    val result: List<FingerprintMatchResult>
) : FingerprintResponse {
    @IgnoredOnParcel override val type: FingerprintTypeResponse = FingerprintTypeResponse.ENROL

}
