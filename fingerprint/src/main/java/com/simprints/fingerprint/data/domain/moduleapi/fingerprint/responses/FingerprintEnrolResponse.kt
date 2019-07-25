package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
class FingerprintEnrolResponse(val guid: String): FingerprintResponse {
    @IgnoredOnParcel override val type: FingerprintResponseType = FingerprintResponseType.ENROL
}
