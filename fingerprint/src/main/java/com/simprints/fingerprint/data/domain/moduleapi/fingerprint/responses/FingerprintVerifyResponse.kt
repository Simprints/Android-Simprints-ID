package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
class FingerprintVerifyResponse(val guid: String,
                                val confidence: Int,
                                val tier: MatchingTier) : FingerprintResponse {
    @IgnoredOnParcel override val type: FingerprintResponseType = FingerprintResponseType.VERIFY
}
