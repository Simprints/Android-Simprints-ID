package com.simprints.id.domain.moduleapi.fingerprint.responses

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintEnrolResponse(val guid: String): FingerprintResponse() {

    @IgnoredOnParcel override val type: FingerprintTypeResponse = FingerprintTypeResponse.ENROL
}
